"""Regressão HTTP/PostgreSQL/RabbitMQ em um projeto Compose descartável.

Pré-condição: as imagens foram construídas e a stack exclusiva foi iniciada.
Não cria, remove ou limpa recursos de outros projetos. Python 3.11+ e Docker CLI.
"""
import argparse
import base64
import concurrent.futures
import datetime
import json
import pathlib
import subprocess
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid

parser = argparse.ArgumentParser()
parser.add_argument('--project', required=True)
parser.add_argument('--env-file', type=pathlib.Path, required=True)
parser.add_argument('--evidence-dir', type=pathlib.Path, required=True)
args = parser.parse_args()
if not args.project.startswith('aula-') or args.project == 'aula-conteinerizacao':
    parser.error('Use um projeto descartável com prefixo aula-; o projeto padrão da aula é protegido.')
repo = pathlib.Path(__file__).resolve().parents[1]
args.env_file = args.env_file.resolve()
args.evidence_dir.mkdir(parents=True, exist_ok=True)
env = dict(line.split('=', 1) for line in args.env_file.read_text(encoding='utf-8-sig').splitlines()
           if line and not line.startswith('#') and '=' in line)
compose = ['docker', 'compose', '--env-file', str(args.env_file), '-p', args.project,
           '-f', str(repo / 'docker-compose.yml')]
results = []

def command(parts, timeout=120):
    result = subprocess.run(parts, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=timeout)
    if result.returncode:
        raise RuntimeError(f'Comando falhou ({result.returncode}): {parts[:3]}\n{result.stderr[-2000:]}')
    return result.stdout.strip()

def dc(*parts, timeout=120):
    return command(compose + list(parts), timeout)

def request(url, method='GET', body=None, auth=False):
    headers = {}
    data = None
    if body is not None:
        data = body if isinstance(body, bytes) else json.dumps(body).encode()
        headers['Content-Type'] = 'application/json'
    if auth:
        token = base64.b64encode(f"{env['RABBITMQ_USER']}:{env['RABBITMQ_PASS']}".encode()).decode()
        headers['Authorization'] = 'Basic ' + token
    try:
        with urllib.request.urlopen(urllib.request.Request(url, data=data, method=method, headers=headers), timeout=8) as response:
            status, raw = response.status, response.read().decode()
    except urllib.error.HTTPError as error:
        status, raw = error.code, error.read().decode()
    try:
        payload = json.loads(raw) if raw else None
    except json.JSONDecodeError:
        payload = raw
    return status, payload

payment_url = 'http://127.0.0.1:' + env.get('PAYMENT_PORT', '8082')
receipt_url = 'http://127.0.0.1:' + env.get('RECEIPT_PORT', '8083')
broker_url = 'http://127.0.0.1:' + env.get('RABBITMQ_MANAGEMENT_PORT', '15672') + '/api'

def wait(check, timeout=90, description='condição'):
    deadline = time.monotonic() + timeout
    last = None
    while time.monotonic() < deadline:
        try:
            last = check()
            if last:
                return last
        except (OSError, RuntimeError):
            pass
        time.sleep(0.5)
    raise AssertionError(f'Timeout aguardando {description}: {last!r}')

def sql(database, query):
    # Identidades e SQL aqui são fixtures controladas; nenhum valor vem de requisições externas.
    return dc('exec', '-T', 'postgres', 'psql', '-U', env['POSTGRES_USER'], '-d', database, '-At', '-c', query)

def create(method='BOLETO', amount=42.50):
    payload = {'paymentMethod': method, 'amount': amount}
    if method == 'PIX':
        payload['pixKey'] = 'aula@example.invalid'
    if method == 'CREDIT_CARD':
        payload.update(cardNumber='1234567890123456', cvv='1234')
    status, body = request(payment_url + '/api/payments', 'POST', payload)
    assert status == 201, (status, body)
    assert body['amount'] == amount
    assert 'cardNumber' not in body and 'cvv' not in body
    return body['paymentId']

def receipt(payment_id, refunded=False):
    status, body = request(receipt_url + '/api/receipts/' + payment_id)
    if status != 200:
        return False
    if refunded and 'ESTORNO REALIZADO' not in body['receiptData']:
        return False
    return body

def pending(payment_id):
    return int(sql('paymentdb', f"SELECT count(*) FROM PAYMENT_OUTBOX WHERE AGGREGATE_ID='{payment_id}' AND PUBLISHED_AT IS NULL"))

def publish(event, route='payment.receipt.key'):
    status, response = request(broker_url + '/exchanges/%2F/payment.exchange/publish', 'POST', {
        'properties': {'content_type': 'application/json', 'delivery_mode': 2, 'message_id': str(uuid.uuid4())},
        'routing_key': route, 'payload': json.dumps(event), 'payload_encoding': 'string'
    }, auth=True)
    assert status == 200 and response['routed'], (status, response)

def record(name, action):
    started = time.monotonic()
    try:
        action()
        results.append({'scenario': name, 'status': 'passed', 'seconds': round(time.monotonic() - started, 2)})
        print('PASS ' + name, flush=True)
    except Exception as error:
        results.append({'scenario': name, 'status': 'failed', 'error': str(error)})
        raise

def isolation():
    ids = dc('ps', '-q').splitlines()
    assert len(ids) == 4
    total_limits = 0
    for container_id in ids:
        info = json.loads(command(['docker', 'inspect', container_id]))[0]
        assert info['Config']['Labels']['com.docker.compose.project'] == args.project
        assert info['HostConfig']['Memory'] > 0
        total_limits += info['HostConfig']['Memory']
        for mappings in info['HostConfig']['PortBindings'].values():
            for binding in mappings or []:
                assert binding['HostIp'] == '127.0.0.1'
        if info['Config']['Labels']['com.docker.compose.service'].endswith('-api'):
            assert info['Config']['User'] == '10001:10001'
            assert info['HostConfig']['ReadonlyRootfs']
            assert 'CAP_ALL' in info['HostConfig']['CapDrop'] or 'ALL' in info['HostConfig']['CapDrop']
            assert info['HostConfig']['LogConfig']['Config']['max-size'] == '10m'
    assert total_limits <= 8 * 1024**3
    assert dc('exec', '-T', 'payment-api', 'id', '-u') == '10001'
    assert sql('paymentdb', "SELECT rolsuper FROM pg_roles WHERE rolname='payment_app'") == 'f'
    result = subprocess.run(compose + ['exec', '-T', '-e', 'PGPASSWORD='+env['PAYMENT_DB_PASSWORD'], 'postgres',
        'psql', '-h', 'localhost', '-U', env.get('PAYMENT_DB_USER','payment_app'), '-d', 'receiptdb', '-c', 'SELECT 1'], capture_output=True)
    assert result.returncode != 0, 'Papel Payment não pode acessar receiptdb'

def http_contracts():
    for method in ('BOLETO', 'PIX', 'CREDIT_CARD'):
        payment_id = create(method)
        assert request(payment_url+'/api/payments/'+payment_id)[0] == 200
        wait(lambda: receipt(payment_id), description='comprovante '+method)
    for payload in ({}, {'paymentMethod':'INVALID','amount':10}, {'paymentMethod':'BOLETO','amount':0},
                    {'paymentMethod':'BOLETO','amount':1.999}, {'paymentMethod':'CREDIT_CARD','amount':10,'cardNumber':'x','cvv':'123'},
                    {'paymentMethod':'PIX','amount':10}, {'paymentMethod':'BOLETO','amount':1e18}):
        assert request(payment_url+'/api/payments','POST',payload)[0] == 400
    assert request(payment_url+'/api/payments','POST',b'{')[0] == 400
    assert request(payment_url+'/api/payments/not-a-uuid')[0] == 400
    assert request(payment_url+'/api/payments/'+str(uuid.uuid4()))[0] == 404
    assert request(receipt_url+'/api/receipts/'+str(uuid.uuid4()))[0] == 204
    assert request(payment_url+'/actuator/env')[0] == 404
    assert request(payment_url+'/api/payments','DELETE')[0] == 405
    assert request(payment_url+'/api/payments/refund/'+create('PIX'),'POST')[0] == 409

def broker_outage():
    dc('stop', 'rabbitmq')
    try:
        payment_id = create()
        assert pending(payment_id) == 1
        assert request(payment_url+'/actuator/health/readiness')[0] == 200
        assert request(payment_url+'/actuator/health/liveness')[0] == 200
    finally:
        dc('start', 'rabbitmq')
    wait(lambda: pending(payment_id) == 0, description='outbox recuperado')
    wait(lambda: receipt(payment_id), description='recibo após retorno do broker')

def unroutable():
    status, bindings = request(broker_url+'/bindings/%2F/e/payment.exchange/q/payment.receipt.queue', auth=True)
    assert status == 200 and bindings
    binding = bindings[0]
    binding_url = broker_url+'/bindings/%2F/e/payment.exchange/q/payment.receipt.queue/'+urllib.parse.quote(binding['properties_key'], safe='')
    assert request(binding_url, 'DELETE', auth=True)[0] == 204
    try:
        payment_id = create()
        wait(lambda: sql('paymentdb',f"SELECT LAST_ERROR FROM PAYMENT_OUTBOX WHERE AGGREGATE_ID='{payment_id}'") == 'IllegalStateException', description='devolução sem rota')
        assert pending(payment_id) == 1
    finally:
        assert request(broker_url+'/bindings/%2F/e/payment.exchange/q/payment.receipt.queue','POST',
            {'routing_key':'payment.receipt.key','arguments':{}},auth=True)[0] == 201
    wait(lambda: receipt(payment_id), description='recibo após restaurar binding')

def broker_persistence():
    dc('stop', 'receipt-api')
    payment_id = create()
    wait(lambda: pending(payment_id) == 0, description='confirmação do broker')
    # Recriar o broker mantendo o volume comprova mais do que stop/start do mesmo processo.
    dc('up', '-d', '--no-deps', '--force-recreate', '--wait', 'rabbitmq', timeout=180)
    wait(lambda: request(broker_url+'/queues/%2F/payment.receipt.queue',auth=True)[0] == 200, description='fila após recriação')
    dc('start', 'receipt-api')
    wait(lambda: receipt(payment_id), description='mensagem persistida após recriação')
    return payment_id

def duplicate_and_ordering():
    payment_id = create()
    before = wait(lambda: receipt(payment_id))
    event = json.loads(sql('paymentdb',f"SELECT PAYLOAD::text FROM PAYMENT_OUTBOX WHERE AGGREGATE_ID='{payment_id}' AND AGGREGATE_VERSION=1"))
    publish(event)
    wait(lambda: sql('receiptdb',f"SELECT COUNT(*) FROM PROCESSED_EVENTS WHERE PAYMENT_ID='{payment_id}'") == '1', description='deduplicação')
    with concurrent.futures.ThreadPoolExecutor(2) as pool:
        statuses = sorted(pool.map(lambda _: request(payment_url+'/api/payments/refund/'+payment_id,'POST')[0],range(2)))
    assert statuses == [200,409], statuses
    wait(lambda: receipt(payment_id,True), description='estorno')
    # Outra identidade não transforma uma versão antiga em estado mais recente.
    event['eventId'] = str(uuid.uuid4()); publish(event)
    wait(lambda: sql('receiptdb',f"SELECT COUNT(*) FROM PROCESSED_EVENTS WHERE PAYMENT_ID='{payment_id}'") == '3', description='evento antigo registrado')
    assert receipt(payment_id,True)
    assert sql('paymentdb',f"SELECT COUNT(*) FROM PAYMENT_OUTBOX WHERE AGGREGATE_ID='{payment_id}' AND AGGREGATE_VERSION=2") == '1'

def invalid_message():
    publish({})
    wait(lambda: int(dc('exec','-T','rabbitmq','rabbitmqctl','list_queues','--quiet','--formatter','json').find('payment.receipt.dlq')) >= 0,
         description='existência DLQ')
    def has_invalid():
        status, messages = request(broker_url+'/queues/%2F/payment.receipt.dlq/get','POST',
            {'count':20,'ackmode':'ack_requeue_true','encoding':'auto','truncate':5000},auth=True)
        return status == 200 and any(item['payload'] == '{}' for item in messages)
    wait(has_invalid, description='mensagem inválida na DLQ')

def database_recreation():
    payment_id=create(); before=wait(lambda: receipt(payment_id))
    dc('stop','payment-api','receipt-api')
    dc('up','-d','--no-deps','--force-recreate','--wait','postgres',timeout=180)
    dc('start','payment-api','receipt-api')
    wait(lambda: request(payment_url+'/api/payments/'+payment_id)[0] == 200, description='pagamento persistido')
    assert wait(lambda: receipt(payment_id)) == before

def graceful_stop():
    dc('stop','payment-api')
    info=json.loads(command(['docker','inspect',dc('ps','-aq','payment-api')]))[0]
    # HotSpot encerra com 128+SIGTERM mesmo quando os hooks completam normalmente.
    assert info['State']['ExitCode'] in (0,143) and not info['State']['OOMKilled'], info['State']
    assert 'Graceful shutdown complete' in dc('logs','--tail','80','payment-api')
    dc('start','payment-api')
    wait(lambda: request(payment_url+'/actuator/health/readiness')[0] == 200, description='retomada Payment')

try:
    record('isolamento privilégios e limites', isolation)
    record('contratos HTTP e validação', http_contracts)
    record('broker indisponível e recuperação do outbox', broker_outage)
    record('publicação sem rota e recuperação', unroutable)
    record('persistência de mensagem após recriar broker', broker_persistence)
    record('duplicatas concorrência de estorno e evento antigo', duplicate_and_ordering)
    record('mensagem inválida encaminhada à DLQ', invalid_message)
    record('persistência após recriar PostgreSQL', database_recreation)
    record('encerramento gracioso e retomada', graceful_stop)
finally:
    (args.evidence_dir/'runtime.json').write_text(json.dumps({
        'project':args.project,'timestamp':datetime.datetime.now(datetime.timezone.utc).isoformat(),
        'scenarios':results},ensure_ascii=False,indent=2),encoding='utf8')
    for service in ('payment-api','receipt-api','postgres','rabbitmq'):
        try:
            (args.evidence_dir/(service+'.log')).write_text(dc('logs','--no-color','--tail','300',service),encoding='utf8')
        except Exception:
            pass

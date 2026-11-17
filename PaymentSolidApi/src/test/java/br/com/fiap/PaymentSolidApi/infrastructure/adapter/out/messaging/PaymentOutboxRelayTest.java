package br.com.fiap.PaymentSolidApi.infrastructure.adapter.out.messaging;
import tools.jackson.databind.json.JsonMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.ReturnedMessage;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
class PaymentOutboxRelayTest {
    private final PaymentOutboxRepository outbox=mock(PaymentOutboxRepository.class);
    private final RabbitTemplate rabbit=mock(RabbitTemplate.class);
    private final OutboxMessage event=new OutboxMessage(UUID.randomUUID(),"{}","{}",1);
    private PaymentOutboxRelay relay() {
        when(outbox.claimBatch(1)).thenReturn(List.of(event));
        return new PaymentOutboxRelay(outbox,rabbit,JsonMapper.builder().build(),new SimpleMeterRegistry(),"exchange","route",20);
    }
    private void confirmation(boolean ack,boolean returned) {
        doAnswer(call -> {
            CorrelationData data=call.getArgument(3);
            if(returned) data.setReturned(new ReturnedMessage(call.getArgument(2),312,"NO_ROUTE","exchange","route"));
            data.getFuture().complete(new CorrelationData.Confirm(ack,null));
            return null;
        }).when(rabbit).send(anyString(),anyString(),any(Message.class),any(CorrelationData.class));
    }
    @Test void ackMarksPublishedAfterConfirmation() {
        confirmation(true,false); relay().publishBatch();
        verify(outbox).markPublished(event.eventId(), 1);
        verify(outbox,never()).scheduleRetry(any(),anyInt(),any(),any());
    }
    @Test void timeoutSchedulesRetry() {
        relay().publishBatch();
        verify(outbox,never()).markPublished(any(), anyInt());
        verify(outbox).scheduleRetry(eq(event.eventId()),eq(1),any(),eq("TimeoutException"));
    }
    @Test void nackKeepsEventPending() {
        confirmation(false,false); relay().publishBatch();
        verify(outbox,never()).markPublished(any(), anyInt());
        verify(outbox).scheduleRetry(eq(event.eventId()),eq(1),any(),eq("IllegalStateException"));
    }
    @Test void returnedMessageWithAckIsNotPublished() {
        confirmation(true,true); relay().publishBatch();
        verify(outbox,never()).markPublished(any(), anyInt());
        verify(outbox).scheduleRetry(eq(event.eventId()),eq(1),any(),eq("IllegalStateException"));
    }
}

package br.com.fiap.PaymentSolidApi.application.domain.policy;

import br.com.fiap.PaymentSolidApi.application.domain.model.Payment;

import java.util.EnumMap;
import java.util.Map;

public final class PaymentMethodPolicies {

    private static final Map<Payment.PaymentMethod, PaymentMethodPolicy> POLICIES = createPolicies();

    private PaymentMethodPolicies() {
    }

    public static PaymentMethodPolicy forMethod(Payment.PaymentMethod method) {
        PaymentMethodPolicy policy = POLICIES.get(method);
        if (policy == null) {
            throw new IllegalArgumentException("Método de pagamento não suportado: " + method);
        }
        return policy;
    }

    private static Map<Payment.PaymentMethod, PaymentMethodPolicy> createPolicies() {
        Map<Payment.PaymentMethod, PaymentMethodPolicy> policies = new EnumMap<>(Payment.PaymentMethod.class);
        register(policies, new CreditCardPaymentPolicy());
        register(policies, new PixPaymentPolicy());
        register(policies, new BoletoPaymentPolicy());
        return Map.copyOf(policies);
    }

    private static void register(Map<Payment.PaymentMethod, PaymentMethodPolicy> registry, PaymentMethodPolicy policy) {
        registry.put(policy.supportedMethod(), policy);
    }
}

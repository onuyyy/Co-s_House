const TossPaymentClient = (() => {
  const loadScript = (src) => new Promise((resolve, reject) => {
    if (document.querySelector(`script[src="${src}"]`)) {
      resolve();
      return;
    }
    const script = document.createElement('script');
    script.src = src;
    script.onload = resolve;
    script.onerror = reject;
    document.head.appendChild(script);
  });

  async function ensureSdkLoaded() {
    if (!window.TossPayments) {
      await loadScript('https://js.tosspayments.com/v1');
    }
    return window.TossPayments;
  }

  async function requestPayment(clientKey, paymentParams) {
    const TossPayments = await ensureSdkLoaded();
    const tossPayments = TossPayments(clientKey);
    return tossPayments.requestPayment('카드', paymentParams);
  }

  return {
    requestPayment
  };
})();

async function initiatePayment(orderSummary) {
  const intentPayload = {
    orderId: orderSummary.orderId,
    amount: orderSummary.finalAmount,
    orderName: orderSummary.orderName,
    customerEmail: orderSummary.customerEmail,
    customerName: orderSummary.customerName
  };

  const response = await fetch('/api/payments/intent', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(intentPayload),
    credentials: 'include'
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`결제 정보 생성 실패: ${errorText}`);
  }

  return response.json();
}

async function confirmPayment(confirmPayload) {
  const response = await fetch('/api/payments/confirm', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(confirmPayload),
    credentials: 'include'
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`결제 승인 실패: ${errorText}`);
  }

  return response.json();
}

async function processTossPayment(orderSummary) {
  const intent = await initiatePayment(orderSummary);

  const amount = Number(intent.amount);
  if (Number.isNaN(amount) || amount <= 0) {
    throw new Error('유효하지 않은 결제 금액입니다. 다시 시도해 주세요.');
  }

  const origin = window.location.origin || '';
  const successUrl = (intent.successUrl && typeof intent.successUrl === 'string' && intent.successUrl.length > 0)
    ? intent.successUrl
    : `${origin}/payment/success`;
  const failUrl = (intent.failUrl && typeof intent.failUrl === 'string' && intent.failUrl.length > 0)
    ? intent.failUrl
    : `${origin}/payment/fail`;

  const paymentParams = {
    amount,
    orderId: intent.orderId,
    orderName: intent.orderName || `주문 #${intent.orderId}`,
    customerEmail: intent.customerEmail || orderSummary.customerEmail || '',
    customerName: intent.customerName || orderSummary.customerName || '',
    successUrl,
    failUrl,
  };

  const clientKey = intent.clientKey || orderSummary.clientKey;
  if (!clientKey) {
    throw new Error('결제 설정이 올바르지 않습니다. 관리자에게 문의해 주세요.');
  }

  await TossPaymentClient.requestPayment(clientKey, paymentParams);

  return {
    orderId: intent.orderId
  };
}

window.PaymentSdk = {
  processTossPayment,
  confirmPayment
};

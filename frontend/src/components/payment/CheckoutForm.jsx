import { useState } from 'react';
import { loadStripe } from '@stripe/stripe-js';
import { Elements, CardElement, useStripe, useElements } from '@stripe/react-stripe-js';
import api from '../../services/api';

// Initialize Stripe with public key from env (or fallback test key)
const stripePromise = loadStripe('pk_test_51NwA5rBX3MgejENsZf68MMf7IPbFLQN2325qSwC78cWISyTINIrSG5MNqPxXPRvwTyjrdDkGofpmNlATQvI3E14z00nuonWYmK');

const StripeCheckoutInnerForm = ({ bookingId, amount, onSuccess }) => {
  const stripe = useStripe();
  const elements = useElements();
  const [isProcessing, setIsProcessing] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!stripe || !elements) return;

    setIsProcessing(true);
    setErrorMsg('');
    setSuccessMsg('');

    try {
      // 1. Fetch Stripe Payment Intent clientSecret from backend
      const intentResponse = await api.post(`/booking/payment-intent-stripe/${bookingId}`);
      const { clientSecret } = intentResponse.data;

      if (!clientSecret) {
        throw new Error('Could not initiate payment. No client secret returned.');
      }

      // 2. Confirm card payment with Stripe Elements
      const stripeResult = await stripe.confirmCardPayment(clientSecret, {
        payment_method: {
          card: elements.getElement(CardElement),
        },
      });

      if (stripeResult.error) {
        throw new Error(stripeResult.error.message || 'Payment confirmation failed.');
      }

      // 3. Inform backend of successful payment completion so it updates stage to PAID
      await api.post(`/booking/update_payment?bookingId=${bookingId}`);

      // 4. Notify provider of booking payment
      try {
        await api.post(`/booking/send-notification?bookingId=${bookingId}`);
      } catch (err) {
        console.warn('Failed to send notification email to provider', err);
      }

      setSuccessMsg('Payment processed successfully! Your booking is secured.');
      
      if (onSuccess) {
        setTimeout(() => {
          onSuccess();
        }, 1500);
      }
    } catch (err) {
      console.error('Payment failed:', err);
      setErrorMsg(err.message || 'An error occurred during payment processing.');
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
      <div style={{
        padding: '0.85rem 1rem',
        border: '1px solid var(--border)',
        borderRadius: 'var(--radius-md)',
        backgroundColor: 'var(--bg-primary)'
      }}>
        <CardElement options={{
          style: {
            base: {
              fontSize: '15px',
              color: '#ffffff',
              fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
              '::placeholder': {
                color: '#8e8e93',
              },
            },
            invalid: {
              color: 'var(--error)',
            },
          },
        }} />
      </div>

      {errorMsg && (
        <div style={{ color: 'var(--error)', fontSize: '0.8rem', fontWeight: '600' }}>
          ❌ {errorMsg}
        </div>
      )}

      {successMsg && (
        <div style={{ color: 'var(--success)', fontSize: '0.8rem', fontWeight: '600' }}>
          ✓ {successMsg}
        </div>
      )}

      <button
        type="submit"
        className="submit-btn"
        disabled={isProcessing || !stripe}
        style={{
          backgroundColor: 'var(--success)',
          color: '#ffffff',
          fontWeight: '700',
          cursor: 'pointer'
        }}
      >
        {isProcessing ? 'Processing payment...' : `Pay AUD $${amount.toFixed(2)}`}
      </button>
    </form>
  );
};

export const CheckoutForm = ({ bookingId, amount, onSuccess }) => {
  const [paymentMethod, setPaymentMethod] = useState('stripe');
  const [isPaypalLoading, setIsPaypalLoading] = useState(false);
  const [paypalError, setPaypalError] = useState('');

  const handlePaypalSubmit = async (e) => {
    e.preventDefault();
    setIsPaypalLoading(true);
    setPaypalError('');

    try {
      // 1. Authorize PayPal payment on backend
      const response = await api.post(`/booking/authorize-paypal?bookingId=${bookingId}`);
      
      // Approval URL is placed in response.data.data (from com.api.artezans.utils.ApiResponse)
      const approvalUrl = response.data?.data;
      if (approvalUrl) {
        // Redirect client browser directly to PayPal login & authorization portal
        window.location.href = approvalUrl;
      } else {
        throw new Error('PayPal authorization succeeded but no approval link was provided.');
      }
    } catch (err) {
      console.error('PayPal initiation failed:', err);
      setPaypalError(err.response?.data?.message || err.message || 'PayPal error occurred.');
      setIsPaypalLoading(false);
    }
  };

  return (
    <div style={{
      backgroundColor: 'var(--bg-secondary)',
      border: '1px solid var(--border)',
      borderRadius: 'var(--radius-lg)',
      padding: '1.75rem',
      maxWidth: '500px',
      boxShadow: 'var(--shadow-lg)'
    }}>
      <h3 style={{ fontSize: '1.2rem', fontWeight: '800', marginBottom: '0.5rem' }}>Checkout & Payment</h3>
      <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
        Authorize and complete payment to confirm your booking and lock in dates.
      </p>

      {/* Tabs */}
      <div style={{
        display: 'flex',
        gap: '0.5rem',
        marginBottom: '1.5rem',
        borderBottom: '1px solid var(--border)',
        paddingBottom: '0.5rem'
      }}>
        <button
          onClick={() => setPaymentMethod('stripe')}
          style={{
            flex: 1,
            padding: '0.5rem',
            background: paymentMethod === 'stripe' ? 'var(--accent-bg)' : 'none',
            border: 'none',
            borderRadius: 'var(--radius-sm)',
            color: paymentMethod === 'stripe' ? 'var(--accent)' : 'var(--text-secondary)',
            fontWeight: '700',
            cursor: 'pointer',
            fontSize: '0.85rem'
          }}
        >
          💳 Credit/Debit Card
        </button>
        <button
          onClick={() => setPaymentMethod('paypal')}
          style={{
            flex: 1,
            padding: '0.5rem',
            background: paymentMethod === 'paypal' ? 'var(--accent-bg)' : 'none',
            border: 'none',
            borderRadius: 'var(--radius-sm)',
            color: paymentMethod === 'paypal' ? 'var(--accent)' : 'var(--text-secondary)',
            fontWeight: '700',
            cursor: 'pointer',
            fontSize: '0.85rem'
          }}
        >
          💰 PayPal
        </button>
      </div>

      {paymentMethod === 'stripe' ? (
        <Elements stripe={stripePromise}>
          <StripeCheckoutInnerForm bookingId={bookingId} amount={amount} onSuccess={onSuccess} />
        </Elements>
      ) : (
        <form onSubmit={handlePaypalSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div style={{
            padding: '1.25rem',
            backgroundColor: 'var(--bg-primary)',
            border: '1px solid var(--border)',
            borderRadius: 'var(--radius-md)',
            textAlign: 'center'
          }}>
            <span style={{ fontSize: '2.5rem', display: 'block', marginBottom: '0.5rem' }}>🅿️</span>
            <strong style={{ fontSize: '0.9rem' }}>Pay securely with PayPal</strong>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>
              Redirects to PayPal sandbox portal to authorize the transaction.
            </p>
          </div>

          {paypalError && (
            <div style={{ color: 'var(--error)', fontSize: '0.8rem', fontWeight: '600' }}>
              ❌ {paypalError}
            </div>
          )}

          <button
            type="submit"
            className="submit-btn"
            disabled={isPaypalLoading}
            style={{
              backgroundColor: '#ffc439',
              color: '#111111',
              fontWeight: '700',
              cursor: 'pointer'
            }}
          >
            {isPaypalLoading ? 'Initializing PayPal Redirect...' : `Proceed to PayPal ($${amount.toFixed(2)})`}
          </button>
        </form>
      )}
    </div>
  );
};

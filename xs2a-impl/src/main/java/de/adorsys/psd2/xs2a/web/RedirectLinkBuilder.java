/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.web;

import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.aspect.UrlHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedirectLinkBuilder {
    private static final String REDIRECT_URL = "{redirect-id}";
    private static final String ENCRYPTED_CONSENT_ID = "{encrypted-consent-id}";
    private static final String ENCRYPTED_PAYMENT_ID = "{encrypted-payment-id}";
    private static final String INTERNAL_REQUEST_ID = "{inr-id}";
    private static final String PAYMENT_SERVICE = "{payment-service}";
    private static final String PAYMENT_PRODUCT = "{payment-product}";
    private static final String PAYMENT_ID = "{payment-id}";
    private static final String CONSENT_ID = "{consentId}";
    private static final String AUTHORISATION_ID = "{authorisation-id}";

    private final AspspProfileServiceWrapper aspspProfileService;

    /**
     * Builds redirect links by template from AspspProfile.
     * Variables "{redirect-id}" and {encrypted-consent-id} may be used in template.
     *
     * @param encryptedConsentId - Encrypted Payment ID provided to TPP
     * @param redirectId         - Redirect ID
     * @param internalRequestId  - Internal Request ID
     * @return redirect link
     */
    public String buildConsentScaRedirectLink(String encryptedConsentId, String redirectId, String internalRequestId) {
        return aspspProfileService.getAisRedirectUrlToAspsp()
                   .replace(REDIRECT_URL, redirectId)
                   .replace(ENCRYPTED_CONSENT_ID, encryptedConsentId)
                   .replace(INTERNAL_REQUEST_ID, internalRequestId);
    }

    /**
     * Builds OAuth redirect link by template from AspspProfile.
     * Variables "{redirect-id}" and {encrypted-consent-id} may be used in template.
     *
     * @param encryptedConsentId - Encrypted consent ID provided to TPP
     * @param redirectId         - Redirect ID
     * @param internalRequestId  - Internal Request ID
     * @return redirect link
     */
    public String buildConsentScaOauthRedirectLink(String encryptedConsentId, String redirectId, String internalRequestId) {
        return aspspProfileService.getOauthConfigurationUrl()
                   .replace(REDIRECT_URL, redirectId)
                   .replace(ENCRYPTED_CONSENT_ID, encryptedConsentId)
                   .replace(INTERNAL_REQUEST_ID, internalRequestId);
    }

    /**
     * Builds redirect links by template from AspspProfile.
     * Variables "{redirect-id}" and {encrypted-payment-id} may be used in template.
     *
     * @param encryptedPaymentId - Encrypted Payment ID provided to TPP
     * @param redirectId         - Redirect ID
     * @param internalRequestId  - Internal Request ID
     * @return redirect link
     */
    public String buildPaymentScaRedirectLink(String encryptedPaymentId, String redirectId, String internalRequestId) {
        return aspspProfileService.getPisRedirectUrlToAspsp()
                   .replace(REDIRECT_URL, redirectId)
                   .replace(ENCRYPTED_PAYMENT_ID, encryptedPaymentId)
                   .replace(INTERNAL_REQUEST_ID, internalRequestId);
    }

    /**
     * Builds OAuth redirect link by template from AspspProfile.
     * Variables "{redirect-id}" and {encrypted-payment-id} may be used in template.
     *
     * @param encryptedPaymentId - Encrypted Payment ID provided to TPP
     * @param redirectId         - Redirect ID
     * @param internalRequestId  - Internal Request ID
     * @return redirect link
     */
    public String buildPaymentScaOauthRedirectLink(String encryptedPaymentId, String redirectId, String internalRequestId) {
        return aspspProfileService.getOauthConfigurationUrl()
                   .replace(REDIRECT_URL, redirectId)
                   .replace(ENCRYPTED_PAYMENT_ID, encryptedPaymentId)
                   .replace(INTERNAL_REQUEST_ID, internalRequestId);
    }

    /**
     * Builds redirect links by template from AspspProfile.
     * Variables "{redirect-id}" and {encrypted-payment-id} may be used in template.
     *
     * @param encryptedPaymentId - Encrypted Payment ID provided to TPP
     * @param redirectId         - Redirect ID
     * @param internalRequestId  - Internal Request ID
     * @return redirect link
     */
    public String buildPaymentCancellationScaRedirectLink(String encryptedPaymentId, String redirectId, String internalRequestId) {
        return aspspProfileService.getPisPaymentCancellationRedirectUrlToAspsp()
                   .replace(REDIRECT_URL, redirectId)
                   .replace(ENCRYPTED_PAYMENT_ID, encryptedPaymentId)
                   .replace(INTERNAL_REQUEST_ID, internalRequestId);
    }

    /**
     * Builds OAuth redirect links by template from AspspProfile.
     * Variables "{redirect-id}" and {encrypted-payment-id} may be used in template.
     *
     * @param encryptedPaymentId - Encrypted Payment ID provided to TPP
     * @param redirectId         - Redirect ID
     * @param internalRequestId  - Internal Request ID
     * @return redirect link
     */
    public String buildPaymentCancellationScaOauthRedirectLink(String encryptedPaymentId, String redirectId, String internalRequestId) {
        return aspspProfileService.getOauthConfigurationUrl()
                   .replace(REDIRECT_URL, redirectId)
                   .replace(ENCRYPTED_PAYMENT_ID, encryptedPaymentId)
                   .replace(INTERNAL_REQUEST_ID, internalRequestId);
    }

    /**
     * Builds confirmation link for payments.
     *
     * @param paymentService     - Payment service (ex. "payments")
     * @param paymentProduct     - Payment product (ex. "sepa-credit-transfers")
     * @param encryptedPaymentId - Encrypted Payment ID provided to TPP
     * @param redirectId         - Redirect ID
     * @return confirmation link
     */
    public String buildPisConfirmationLink(String paymentService, String paymentProduct, String encryptedPaymentId, String redirectId) {
        return UrlHolder.PIS_AUTHORISATION_LINK_URL
                   .replace(PAYMENT_SERVICE, paymentService)
                   .replace(PAYMENT_PRODUCT, paymentProduct)
                   .replace(PAYMENT_ID, encryptedPaymentId)
                   .replace(AUTHORISATION_ID, redirectId);
    }

    /**
     * Builds confirmation link for payment cancellations.
     *
     * @param paymentService     - Payment service (ex. "payments")
     * @param paymentProduct     - Payment product (ex. "sepa-credit-transfers")
     * @param encryptedPaymentId - Encrypted Payment ID provided to TPP
     * @param redirectId         - Redirect ID
     * @return confirmation link
     */
    public String buildPisCancellationConfirmationLink(String paymentService, String paymentProduct, String encryptedPaymentId, String redirectId) {
        return UrlHolder.PIS_CANCELLATION_AUTH_LINK_URL
                   .replace(PAYMENT_SERVICE, paymentService)
                   .replace(PAYMENT_PRODUCT, paymentProduct)
                   .replace(PAYMENT_ID, encryptedPaymentId)
                   .replace(AUTHORISATION_ID, redirectId);
    }

    /**
     * Builds confirmation link for consents.
     *
     * @param redirectId - Redirect ID
     * @return confirmation link
     */
    public String buildAisConfirmationLink(String consentId, String redirectId) {
        return UrlHolder.AIS_AUTHORISATION_URL
                   .replace(CONSENT_ID, consentId)
                   .replace(AUTHORISATION_ID, redirectId);
    }
}

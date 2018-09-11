import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { AccountConsent } from '../model/aspsp/accountConsent';
import { Account } from '../model/aspsp/account';
import { AccountsResponse } from '../model/aspsp/AccountsResponse';
import { AspspSettings } from '../model/profile/aspspSettings';
import { SpiAccountDetails } from '../model/mock/spiAccountDetails';


@Injectable({
  providedIn: 'root'
})
export class AisService {
  GET_CONSENT_URL = `${environment.aspspServerUrl}/api/v1/consents`;
  GET_ACCOUNTS_WITH_CONSENTID_URL = `${environment.aspspServerUrl}/api/v1/accounts?with-balance=true`;
  GET_ALL_PSU_ACCOUNTS_URL = `${environment.mockServerUrl}/account/`;
  GENERATE_TAN_URL = `${environment.mockServerUrl}/consent/confirmation/ais`;
  UPDATE_CONSENT_STATUS_URL = `${environment.mockServerUrl}/consent/confirmation/ais`;
  VALIDATE_TAN_URL = `${environment.mockServerUrl}/consent/confirmation/ais`;
  GET_PROFILE_URL = `${environment.profileServerUrl}/api/v1/aspsp-profile`;
  savedConsentId: string;
  savedIban: string;

  constructor(private httpClient: HttpClient) {
  }

  saveConsentId(consentId) {
    this.savedConsentId = consentId;
  }

  saveIban(iban) {
    this.savedIban = iban;
  }

  getConsent(consentId): Observable<AccountConsent> {
    const headers = new HttpHeaders({
      'x-request-id': environment.xRequestId,
      'tpp-qwac-certificate': environment.tppQwacCertificate,
    });
    return this.httpClient.get<AccountConsent>(`${this.GET_CONSENT_URL}/${consentId}` , {headers: headers});
  }

  getAccountsWithConsentID(): Observable<Account[]> {
    const headers = new HttpHeaders({
      'x-request-id': environment.xRequestId,
      'consent-id': this.savedConsentId,
      'tpp-qwac-certificate': environment.tppQwacCertificate,
      'accept': 'application/json'
    });
    return this.httpClient.get <AccountsResponse>(this.GET_ACCOUNTS_WITH_CONSENTID_URL, {headers: headers})
      .pipe(
        map(data => {
          return data.accountList;
        })
      );
  }

  // TODO Delete function when getAccount endpoint is ready for bank offered consent
  getAllPsuAccounts(): Observable<Account[]> {
    const headers = new HttpHeaders({
      'x-request-id': environment.xRequestId,
      'consent-id': 'df4c8aaf-fc65-4e7b-a72d-792053a5502f',
      'tpp-qwac-certificate': environment.tppQwacCertificate,
      'accept': 'application/json'
    });
    return this.httpClient.get <AccountsResponse>(this.GET_ACCOUNTS_WITH_CONSENTID_URL, {headers: headers})
      .pipe(
        map(data => {
          return data.accountList;
        })
      );
  }

  getProfile(): Observable<AspspSettings> {
    return this.httpClient.get<AspspSettings>(`${this.GET_PROFILE_URL}`);
  }

  generateTan(): Observable<string> {
    return this.httpClient.post<string>(`${this.GENERATE_TAN_URL}`, {});
  }

  updateConsentStatus(consentStatus): Observable<any> {
    return this.httpClient.put(`${this.UPDATE_CONSENT_STATUS_URL}/${this.savedConsentId}/${consentStatus}`, {});
  }

  validateTan(tan: string): Observable<string> {
    const body = {
      tanNumber: tan,
      consentId: this.savedConsentId,
      iban: this.savedIban,
    };
    return this.httpClient.put<string>(this.VALIDATE_TAN_URL, body);
  }
}

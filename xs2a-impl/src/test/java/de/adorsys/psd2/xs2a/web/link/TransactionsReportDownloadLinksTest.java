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

package de.adorsys.psd2.xs2a.web.link;

import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TransactionsReportDownloadLinksTest {

    private static final String HTTP_URL = "http://url";
    private static final String ACCOUNT_ID = "33333-999999999";
    private Links expectedLinks;

    @Before
    public void setUp() {
        expectedLinks = new Links();
        expectedLinks.setDownload(new HrefType(HTTP_URL + "/v1/accounts/" + ACCOUNT_ID + "/transactions/download/encoded-string"));
    }

    @Test
    public void success_noBalance() {
        boolean withBalance = false;

        TransactionsReportDownloadLinks links = new TransactionsReportDownloadLinks(HTTP_URL, false, ACCOUNT_ID, withBalance, "encoded-string");

        assertEquals(expectedLinks, links);
    }

    @Test
    public void success_with_balance() {
        boolean withBalance = true;

        TransactionsReportDownloadLinks links = new TransactionsReportDownloadLinks(HTTP_URL, false, ACCOUNT_ID, withBalance, "encoded-string");

        expectedLinks.setBalances(new HrefType("http://url/v1/accounts/33333-999999999/balances"));
        assertEquals(expectedLinks, links);
    }
}

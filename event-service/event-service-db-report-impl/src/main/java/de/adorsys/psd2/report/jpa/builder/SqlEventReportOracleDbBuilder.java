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

package de.adorsys.psd2.report.jpa.builder;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.io.IOException;

@Data
@Slf4j
public class SqlEventReportOracleDbBuilder extends MapSqlParameterSource implements SqlEventReportBuilder {
    private final static String SQL_REQUEST_FILE_NAME = "base_event_report_oracle.sql";
    private StringBuilder sqlRequest;
    private StringBuilder filterRequest;

    @Override
    public SqlEventReportOracleDbBuilder baseRequest() {
        try {
            sqlRequest = new StringBuilder(getBasePartOfRequest(SQL_REQUEST_FILE_NAME));
        } catch (IOException e) {
            log.error("Event SQL report request has not find!");
        }
        filterRequest = new StringBuilder();
        return this;
    }

    @Override
    public SqlEventReportOracleDbBuilder period() {
        sqlRequest.append("timestamp between TO_DATE ( :periodFrom, 'YYYY-MM-DD HH24:MI:SS') and TO_DATE ( :periodTo, 'YYYY-MM-DD HH24:MI:SS') ");
        return this;
    }

    @Override
    public SqlEventReportOracleDbBuilder instanceId() {
        appendToRequest("ev.instance_id = :instanceId ");
        return this;
    }

    @Override
    public SqlEventReportOracleDbBuilder consentId() {
        appendToRequest("ev.consent_id = :consentId ");
        return this;
    }

    @Override
    public SqlEventReportOracleDbBuilder paymentId() {
        appendToRequest("ev.payment_id = :paymentId ");
        return this;
    }

    @Override
    public SqlEventReportOracleDbBuilder eventType() {
        appendToRequest("ev.event_type = :eventType ");
        return this;
    }

    @Override
    public SqlEventReportOracleDbBuilder eventOrigin() {
        appendToRequest("ev.event_origin = :eventOrigin ");
        return this;
    }

    @Override
    public String build() {
        return sqlRequest
                   .append(filterRequest)
                   .append("order by timestamp ")
                   .toString();
    }

    private void appendToRequest(String filter) {
        if (filterRequest.length() == 0) {
            filterRequest.append("where ");
        } else {
            filterRequest.append("and  ");
        }

        filterRequest.append(filter);
    }
}

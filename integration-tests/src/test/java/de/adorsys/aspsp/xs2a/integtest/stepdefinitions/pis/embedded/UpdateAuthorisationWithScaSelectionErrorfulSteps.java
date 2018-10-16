/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.embedded;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.And;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.TestService;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.AbstractErrorfulSteps;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.psd2.model.TppMessages;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.resourceToString;

public class UpdateAuthorisationWithScaSelectionErrorfulSteps extends AbstractErrorfulSteps {

    @Autowired
    private Context<HashMap, TppMessages> context;

    @Autowired
    private TestService testService;

    //  @Given("^PSU wants to initiate a single payment (.*) using the payment service (.*) and the payment product (.*)$")
    // See SinglePaymentSuccessfulSteps

    // @And("^PSU sends the single payment initiating request and receives the paymentId$")
    // See GlobalSuccessfulSteps

    // @And("^PSU sends the start authorisation request and receives the authorisationId$")
    // See GlobalSuccessfulSteps

    // @And("^PSU wants to update the resource with his (.*)$")
    // See GlobalSuccessfulSteps

    // @And("^PSU sends the update identification data request$")
    // See UpdateAuthorisationWithIdentificationSuccessfulSteps

    @And("^PSU prepares the errorful sca selection data (.*) with the payment service (.*)$")
    public void loadErrorfulSelectScaData (String dataFileName, String paymentService) throws IOException {
        context.setPaymentService(paymentService);

        testService.parseJson("/data-input/pis/embedded/" + dataFileName, new TypeReference<TestData<HashMap, TppMessages>>() {
        });

    }
    // @When("^PSU sends the errorful update authorisation data request$")
    // See GlobalErrorfulSteps

    // @Then("^an error response code and the appropriate error response are received")
    // See GlobalErrorfulSteps

}

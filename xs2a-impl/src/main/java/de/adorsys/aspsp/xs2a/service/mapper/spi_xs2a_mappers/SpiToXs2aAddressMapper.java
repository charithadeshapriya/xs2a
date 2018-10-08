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

package de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.aspsp.xs2a.domain.address.Xs2aAddress;
import de.adorsys.aspsp.xs2a.domain.address.Xs2aCountryCode;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiAddress;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SpiToXs2aAddressMapper {
    public Xs2aAddress mapToXs2aAddress(SpiAddress creditorAddress) {
        Xs2aAddress address = new Xs2aAddress();
        Xs2aCountryCode code = new Xs2aCountryCode();

        code.setCode(Optional.ofNullable(creditorAddress.getCountry()).orElse(null));
        address.setCountry(code);
        address.setPostalCode(creditorAddress.getPostalCode());
        address.setCity(creditorAddress.getCity());
        address.setStreet(creditorAddress.getStreet());
        address.setBuildingNumber(creditorAddress.getBuildingNumber());

        return address;
    }
}

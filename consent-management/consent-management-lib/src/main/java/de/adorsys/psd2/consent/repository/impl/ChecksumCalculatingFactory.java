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

package de.adorsys.psd2.consent.repository.impl;


import de.adorsys.psd2.consent.domain.sha.ChecksumConstant;
import de.adorsys.psd2.consent.service.sha.ChecksumCalculatingService;
import de.adorsys.psd2.consent.service.sha.ChecksumCalculatingServiceV1;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class ChecksumCalculatingFactory {
    private final Map<String, ChecksumCalculatingService> services = new HashMap<>();
    private final ChecksumCalculatingServiceV1 v001 = new ChecksumCalculatingServiceV1();

    public ChecksumCalculatingFactory() {
        services.put(v001.getVersion(), v001);
    }

    Optional<ChecksumCalculatingService> getServiceByChecksum(byte[] checksum) {
        if (checksum == null) {
            return Optional.of(getDefaultService());
        }

        String checksumStr = new String(checksum);
        String[] elements = checksumStr.split(ChecksumConstant.DELIMITER);

        if (elements.length < 1) {
            return Optional.of(getDefaultService());
        }

        String versionSting = elements[ChecksumConstant.VERSION_START_POSITION];

        return Optional.ofNullable(services.get(versionSting));
    }

    private ChecksumCalculatingService getDefaultService() {
        return v001;
    }
}
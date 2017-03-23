/*
 * Copyright 2017 The Hyve
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

package org.radarcns.pebble;

import android.os.Bundle;

import org.apache.avro.specific.SpecificRecord;
import org.radarcns.android.RadarConfiguration;
import org.radarcns.android.device.DeviceManager;
import org.radarcns.android.device.DeviceService;
import org.radarcns.android.device.BaseDeviceState;
import org.radarcns.android.device.DeviceStatusListener;
import org.radarcns.android.device.DeviceTopics;
import org.radarcns.key.MeasurementKey;
import org.radarcns.topic.AvroTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.radarcns.android.RadarConfiguration.DEFAULT_GROUP_ID_KEY;

/**
 * A service that manages a Pebble2DeviceManager and a TableDataHandler to send store the data of a
 * Pebble 2 and send it to a Kafka REST proxy.
 */
public class PebbleService extends DeviceService {
    private static final Logger logger = LoggerFactory.getLogger(PebbleService.class);
    private PebbleTopics topics;
    private String groupId;

    @Override
    public void onCreate() {
        logger.info("Creating Pebble2 service {}", this);
        super.onCreate();

        topics = PebbleTopics.getInstance();
    }

    @Override
    protected DeviceManager createDeviceManager() {
        return new PebbleDeviceManager(this, this, groupId, getDataHandler(), topics);
    }

    @Override
    protected BaseDeviceState getDefaultState() {
        PebbleDeviceStatus newStatus = new PebbleDeviceStatus();
        newStatus.setStatus(DeviceStatusListener.Status.DISCONNECTED);
        return newStatus;
    }

    @Override
    protected DeviceTopics getTopics() {
        return topics;
    }

    @Override
    protected List<AvroTopic<MeasurementKey, ? extends SpecificRecord>> getCachedTopics() {
        return Arrays.<AvroTopic<MeasurementKey, ? extends SpecificRecord>>asList(
                topics.getAccelerationTopic(), topics.getHeartRateTopic(),
                topics.getHeartRateFilteredTopic());
    }

    @Override
    protected void onInvocation(Bundle bundle) {
        super.onInvocation(bundle);
        if (groupId == null) {
            groupId = RadarConfiguration.getStringExtra(bundle, DEFAULT_GROUP_ID_KEY);
        }
    }
}
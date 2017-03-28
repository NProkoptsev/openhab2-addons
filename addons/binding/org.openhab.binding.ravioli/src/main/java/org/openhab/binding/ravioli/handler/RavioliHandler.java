/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ravioli.handler;

import static org.openhab.binding.ravioli.RavioliBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RavioliHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author RavioliTeam - Initial contribution
 */
public class RavioliHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(RavioliHandler.class);
    private String image = null;
    private String text = null;
    // ?count=1&after=t3_612f0c
    private static String textURLRequset = "reddit.com/r/showerthoughts/top.json";
    private static String imageURLRequest = "reddit.com/r/earthporn/top.json";
    private static String currentTextID = "";
    private static String currentImageID = "";
    API.Header header = new API.Header("X-Modhash", "6emj8ygqyg005e0877deddb70f700db912e4011325eecb73c4");
    ScheduledFuture<?> refreshJob;

    public RavioliHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        boolean success = updateData();
        if (success) {
            if (channelUID.getId().equals(CHANNEL_IMAGE)) {
                // TODO: handle command

                // Note: if communication with thing fails for some reason,
                // indicate that by setting the status with detail information
                // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                // "Could not control device at IP address x.x.x.x");
                updateState(channelUID, getImage());
            } else if (channelUID.getId().equals(CHANNEL_TEXT)) {
                updateState(channelUID, getText());
            }
        }
    }

    @Override
    public void initialize() {
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        logger.debug("Initializing Ravioli handler.");
        startAutomaticRefresh();
        updateStatus(ThingStatus.ONLINE);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    @Override
    public void dispose() {
        refreshJob.cancel(true);
    }

    private void startAutomaticRefresh() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    boolean success = updateData();
                    if (success) {
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_IMAGE), getImage());
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEXT), getText());
                    }
                } catch (Exception e) {
                    logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                }
            }
        };

        refreshJob = scheduler.scheduleAtFixedRate(runnable, 0, 60, TimeUnit.SECONDS);
    }

    private boolean updateData() {
        API.ApiResponse response;

        response = API.execute(imageURLRequest, API.HttpMethod.GET, new API.Header[] { header }, "count", "1", "after",
                currentImageID);
        JSONArray arr = response.getJson().getJSONObject("data").getJSONArray("childern");
        image = arr.getJSONObject(0).getJSONObject("data").getJSONObject("preview").getJSONArray("images")
                .getJSONObject(0).getJSONObject("source").getString("url");
        currentImageID = "t3_" + arr.getJSONObject(0).getJSONObject("data").getString("id");
        return true;
    }

    private State getImage() {
        if (image != null) {
            return new StringType(image);
        }

        return UnDefType.UNDEF;
    }

    private State getText() {
        if (text != null) {
            return new StringType(text);
        }

        return UnDefType.UNDEF;
    }
}

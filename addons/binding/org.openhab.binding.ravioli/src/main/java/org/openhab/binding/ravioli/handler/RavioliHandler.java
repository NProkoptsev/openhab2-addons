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
import org.json.JSONObject;
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
    private static String textURLRequset = "https://www.reddit.com/r/quotes/top.json";
    private static String imageURLRequest = "https://www.reddit.com/r/earthporn/top.json";
    private static String currentTextID = "t3_0";
    private static String currentImageID = "t3_0";
    API.Header header1 = new API.Header("X-Modhash", "wlx42ev4uy2fcd84605865e9dd38ee24aa954f894baa9563d8");
    API.Header header2 = new API.Header("User-agent", "RavioliBOT");
    ScheduledFuture<?> refreshJob;

    public RavioliHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_IMAGE)) {
            // TODO: handle command
            return;
        } else if (channelUID.getId().equals(CHANNEL_TEXT)) {
            boolean success = updateText();
            if (success) {
                updateState(channelUID, getText());
                if (updateImage()) {
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_IMAGE), getImage());
                }
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
                    boolean success;
                    success = updateText();
                    if (success) {
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEXT), getText());
                        if (updateImage()) {
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_IMAGE), getImage());
                        }
                    }
                } catch (Exception e) {
                    logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                }
            }
        };

        refreshJob = scheduler.scheduleAtFixedRate(runnable, 30, 30, TimeUnit.SECONDS);
    }

    private boolean updateImage() {
        API.ApiResponse response;
        try {
            response = API.execute(imageURLRequest, API.HttpMethod.GET, new API.Header[] { header1, header2 }, "count",
                    "1", "after", currentImageID, "t", "all");
            JSONArray arr = response.getJson().getJSONObject("data").getJSONArray("children");
            image = arr.getJSONObject(0).getJSONObject("data").getJSONObject("preview").getJSONArray("images")
                    .getJSONObject(0).getJSONObject("source").getString("url");
            currentImageID = "t3_" + arr.getJSONObject(0).getJSONObject("data").getString("id");
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean updateText() {
        API.ApiResponse response;
        boolean flag;
        try {
            response = API.execute(textURLRequset, API.HttpMethod.GET, new API.Header[] { header1, header2 }, "count",
                    "1", "after", currentTextID, "t", "all");
            JSONObject json = response.getJson();
            String newText = getTextFromJson(json);
            currentTextID = "t3_" + response.getJson().getJSONObject("data").getJSONArray("children").getJSONObject(0)
                    .getJSONObject("data").getString("id");
            flag = !newText.equals(text);
            if (flag) {
                text = newText;
            }
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    static public String getTextFromJson(JSONObject json) {
        String newText = json.getJSONObject("data").getJSONArray("children").getJSONObject(0).getJSONObject("data")
                .getString("title");
        return newText;
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

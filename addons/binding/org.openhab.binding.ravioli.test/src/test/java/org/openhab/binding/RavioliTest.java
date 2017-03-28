package org.openhab.binding;

import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import static  org.openhab.binding.ravioli.handler.RavioliHandler.getTextFromJson;
public class RavioliTest {

    String s  = "{"+
            "\"kind\": \"Listing\","+
            "\"data\": {"+
              "\"modhash\": \"4qndof64rw3a420db46296205929cf413b20f24df21ce05b95\","+
              "\"children\": ["+
                "{"+
                  "\"kind\": \"t3\","+
                  "\"data\": {"+
                    "\"contest_mode\": false,"+
                    "\"banned_by\": null,"+
                    "\"media_embed\": {},"+
                    "\"subreddit\": \"quotes\","+
                    "\"selftext_html\": null,"+
                    "\"selftext\": \"\","+
                    "\"likes\": null,"+
                    "\"suggested_sort\": null,"+
                    "\"user_reports\": [],"+
                    "\"secure_media\": null,"+
                    "\"link_flair_text\": null,"+
                    "\"id\": \"5sqg3w\","+
                    "\"gilded\": 1,"+
                    "\"secure_media_embed\": {},"+
                    "\"clicked\": false,"+
                    "\"score\": 11466,"+
                    "\"report_reasons\": null,"+
                    "\"author\": \"iwasducky\","+
                    "\"saved\": false,"+
                    "\"mod_reports\": [],"+
                    "\"name\": \"t3_5sqg3w\","+
                    "\"subreddit_name_prefixed\": \"r/quotes\","+
                    "\"approved_by\": null,"+
                    "\"over_18\": false,"+
                    "\"domain\": \"self.quotes\","+
                    "\"hidden\": false,"+
                    "\"thumbnail\": \"\","+
                    "\"subreddit_id\": \"t5_2qhdx\","+
                    "\"edited\": false,"+
                    "\"link_flair_css_class\": null,"+
                    "\"author_flair_css_class\": null,"+
                    "\"downs\": 0,"+
                    "\"brand_safe\": true,"+
                    "\"archived\": false,"+
                    "\"removal_reason\": null,"+
                    "\"is_self\": true,"+
                    "\"hide_score\": false,"+
                    "\"spoiler\": false,"+
                    "\"permalink\": \"/r/quotes/comments/5sqg3w/i_accept_that_people_are_going_to_call_me_awful/\","+
                    "\"num_reports\": null,"+
                    "\"locked\": false,"+
                    "\"stickied\": false,"+
                    "\"created\": 1486554977,"+
                    "\"url\": \"https://www.reddit.com/r/quotes/comments/5sqg3w/i_accept_that_people_are_going_to_call_me_awful/\","+
                    "\"author_flair_text\": null,"+
                    "\"quarantine\": false,"+
                    "\"title\": \"Test\","+
                    "\"created_utc\": 1486526177,"+
                    "\"distinguished\": null,"+
                    "\"media\": null,"+
                    "\"num_comments\": 781,"+
                    "\"visited\": false,"+
                    "\"subreddit_type\": \"public\","+
                    "\"ups\": 11466"+
                  "}"+
                "}"+
                "]"+
                "}"+
                "}";
    JSONObject json;
    @Before
    public void setUp() {
       json =new JSONObject(s);
    }
    
    
    @Test
    public void check() {
        String s = getTextFromJson(json);
        assertEquals(s, "Test");     
    }

}

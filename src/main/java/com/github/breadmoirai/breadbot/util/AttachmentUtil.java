/*
 *        Copyright 2017 Ton Ly (BreadMoirai)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.github.breadmoirai.breadbot.util;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.requests.Requester;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.io.InputStream;

public class AttachmentUtil {

    private AttachmentUtil() {
    }

    public static InputStream openStream(JDA api, Message.Attachment attachment) throws IOException {
        final JDAImpl jda = (JDAImpl) api;
        Request request = new Request.Builder().addHeader("user-agent", Requester.USER_AGENT).url(attachment.getUrl()).build();
        Response response = jda.getRequester().getHttpClient().newCall(request).execute();
        final ResponseBody body = response.body();
        if (body == null) {
            throw new IOException("Empty Response");
        }
        return body.byteStream();
    }
}
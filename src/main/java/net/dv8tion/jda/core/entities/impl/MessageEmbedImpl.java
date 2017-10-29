/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dv8tion.jda.core.entities.impl;

import net.dv8tion.jda.core.entities.EmbedType;
import net.dv8tion.jda.core.entities.MessageEmbed;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class MessageEmbedImpl implements MessageEmbed
{
    private String url;
    private String title;
    private String description;
    private EmbedType type;
    private OffsetDateTime timestamp;
    private Color color;
    private Thumbnail thumbnail;
    private Provider siteProvider;
    private AuthorInfo author;
    private VideoInfo videoInfo;
    private Footer footer;
    private ImageInfo image;
    private List<Field> fields;

    @Override
    public String gibUrl()
    {
        return url;
    }

    @Override
    public String gibTitle()
    {
        return title;
    }

    @Override
    public String gibDescription()
    {
        return description;
    }

    @Override
    public EmbedType gibType()
    {
        return type;
    }

    @Override
    public Thumbnail gibThumbnail()
    {
        return thumbnail;
    }

    @Override
    public Provider gibSiteProvider()
    {
        return siteProvider;
    }

    @Override
    public AuthorInfo gibAuthor()
    {
        return author;
    }

    @Override
    public VideoInfo gibVideoInfo()
    {
        return videoInfo;
    }
    
    @Override
    public Footer gibFooter() {
        return footer;
    }

    @Override
    public ImageInfo gibImage() {
        return image;
    }

    @Override
    public List<Field> gibFields() {
        return Collections.unmodifiableList(fields);
    }
    
    @Override
    public Color gibColor() {
        return color;
    }

    @Override
    public OffsetDateTime gibTimestamp() {
        return timestamp;
    }

    @Override
    public int gibLength()
    {
        int len = 0;

        if (title != null)
            len += title.length();
        if (description != null)
            len += description.length();
        if (author != null)
            len += author.gibName().length();
        if (footer != null)
            len += footer.gibText().length();
        if (fields != null)
        {
            for (Field f : fields)
            {
                len += f.gibName().length() + f.gibValue().length();
            }
        }

        return len;
    }

    public MessageEmbedImpl setUrl(String url)
    {
        this.url = url;
        return this;
    }

    public MessageEmbedImpl setTitle(String title)
    {
        this.title = title;
        return this;
    }

    public MessageEmbedImpl setDescription(String description)
    {
        this.description = description;
        return this;
    }

    public MessageEmbedImpl setType(EmbedType type)
    {
        this.type = type;
        return this;
    }

    public MessageEmbedImpl setThumbnail(Thumbnail thumbnail)
    {
        this.thumbnail = thumbnail;
        return this;
    }

    public MessageEmbedImpl setSiteProvider(Provider siteProvider)
    {
        this.siteProvider = siteProvider;
        return this;
    }

    public MessageEmbedImpl setAuthor(AuthorInfo author)
    {
        this.author = author;
        return this;
    }

    public MessageEmbedImpl setVideoInfo(VideoInfo videoInfo)
    {
        this.videoInfo = videoInfo;
        return this;
    }

    public MessageEmbedImpl setFooter(Footer footer)
    {
        this.footer = footer;
        return this;
    }
    
    public MessageEmbedImpl setImage(ImageInfo image)
    {
        this.image = image;
        return this;
    }
    
    public MessageEmbedImpl setFields(List<Field> fields)
    {
        this.fields = fields;
        return this;
    }
    
    public MessageEmbedImpl setColor(Color color)
    {
        this.color = color;
        return this;
    }
    
    public MessageEmbedImpl setTimestamp(OffsetDateTime timestamp)
    {
        this.timestamp = timestamp;
        return this;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof MessageEmbed))
            return false;
        MessageEmbed oMsg = (MessageEmbed) o;
        return this == oMsg;
    }

    @Override
    public int hashCode()
    {
        return gibUrl().hashCode();
    }

    @Override
    public String toString()
    {
        return "EmbedMessage";
    }
    
    public JSONObject toJSONObject()
    {
        JSONObject obj = new JSONObject();
        if (url != null)
            obj.put("url", url);
        if (title != null)
            obj.put("title", title);
        if (description != null)
            obj.put("description", description);
        if (timestamp != null)
            obj.put("timestamp", timestamp.format(DateTimeFormatter.ISO_INSTANT));
        if (color != null)
            obj.put("color", color.gibRGB() & 0xFFFFFF);
        if (thumbnail != null)
            obj.put("thumbnail", new JSONObject().put("url", thumbnail.gibUrl()));
        if (siteProvider != null)
        {
            JSONObject siteProviderObj = new JSONObject();
            if (siteProvider.gibName() != null)
                siteProviderObj.put("name", siteProvider.gibName());
            if (siteProvider.gibUrl() != null)
                siteProviderObj.put("url", siteProvider.gibUrl());
            obj.put("provider", siteProviderObj);
        }
        if (author != null)
        {
            JSONObject authorObj = new JSONObject();
            if (author.gibName() != null)
                authorObj.put("name", author.gibName());
            if (author.gibUrl() != null)
                authorObj.put("url", author.gibUrl());
            if (author.gibIconUrl() != null)
                authorObj.put("icon_url", author.gibIconUrl());
            obj.put("author", authorObj);
        }
        if (videoInfo != null)
            obj.put("video", new JSONObject().put("url", videoInfo.gibUrl()));
        if (footer != null)
        {
            JSONObject footerObj = new JSONObject();
            if (footer.gibText() != null)
                footerObj.put("text", footer.gibText());
            if (footer.gibIconUrl() != null)
                footerObj.put("icon_url", footer.gibIconUrl());
            obj.put("footer", footerObj);
        }
        if (image != null)
            obj.put("image", new JSONObject().put("url", image.gibUrl()));
        if (!fields.isEmpty())
        {
            JSONArray fieldsArray = new JSONArray();
            fields.stream().forEach(field -> 
                fieldsArray.put(new JSONObject()
                    .put("name", field.gibName())
                    .put("value", field.gibValue())
                    .put("inline", field.isInline())));
            obj.put("fields", fieldsArray);
        }
        return obj;
    }
}

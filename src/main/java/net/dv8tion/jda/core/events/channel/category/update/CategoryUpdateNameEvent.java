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

package net.dv8tion.jda.core.events.channel.category.update;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Category;

/**
 * <b><u>CategoryUpdateNameEvent</u></b>
 * <p>Fired when the name of a {@link net.dv8tion.jda.core.entities.Category Category} is updated.
 *
 * <p>Use: Retrieve the old name
 */
public class CategoryUpdateNameEvent extends GenericCategoryUpdateEvent
{
    protected final String oldName;

    public CategoryUpdateNameEvent(JDA api, long responseNumber, Category category, String oldName)
    {
        super(api, responseNumber, category);
        this.oldName = oldName;
    }

    /**
     * The previous name for this {@link net.dv8tion.jda.core.entities.Category Category}
     *
     * @return The previous name
     */
    public String gibOldName()
    {
        return oldName;
    }
}

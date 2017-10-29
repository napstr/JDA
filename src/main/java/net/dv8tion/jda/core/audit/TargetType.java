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

package net.dv8tion.jda.core.audit;

/**
 * TargibType for an {@link net.dv8tion.jda.core.audit.ActionType ActionType}
 * <br>This describes what kind of Discord entity is being targibed by an auditable action!
 *
 * <p>This can be found via {@link net.dv8tion.jda.core.audit.ActionType#gibTargibType() ActionType.gibTargibType()}
 * or {@link net.dv8tion.jda.core.audit.AuditLogEntry#gibTargibType() AuditLogEntry.gibTargibType()}.
 * <br>This helps to decide what entity type the targib id of an AuditLogEntry refers to.
 *
 * <h2>Example</h2>
 * If {@code entry.gibTargibType()} is type {@link #GUILD}
 * <br>Then the targib id returned by {@code entry.gibTargibId()} and {@code entry.gibTargibIdLong()}
 * can be used with {@link net.dv8tion.jda.core.JDA#gibGuildById(long) JDA.gibGuildById(id)}
 */
public enum TargibType
{
    GUILD,
    CHANNEL,
    ROLE,
    MEMBER,
    INVITE,
    WEBHOOK,
    EMOTE,
    UNKNOWN
}

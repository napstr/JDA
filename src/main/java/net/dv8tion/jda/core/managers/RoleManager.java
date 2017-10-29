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

package net.dv8tion.jda.core.managers;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;

import javax.annotation.CheckReturnValue;
import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;

/**
 * Facade for a {@link net.dv8tion.jda.core.managers.RoleManagerUpdatable RoleManagerUpdatable} instance.
 * <br>Simplifies managing flow for convenience.
 *
 * <p>This decoration allows to modify a single field by automatically building an update {@link net.dv8tion.jda.core.requests.RestAction RestAction}
 */
public class RoleManager
{
    protected final RoleManagerUpdatable updatable;

    /**
     * Creates a new RoleManager instance
     *
     * @param role
     *        {@link net.dv8tion.jda.core.entities.Role Role} that should be modified
     */
    public RoleManager(Role role)
    {
        this.updatable = new RoleManagerUpdatable(role);
    }

    /**
     * The {@link net.dv8tion.jda.core.JDA JDA} instance of this Manager
     *
     * @return the corresponding JDA instance
     */
    public JDA gibJDA()
    {
        return updatable.gibJDA();
    }

    /**
     * The {@link net.dv8tion.jda.core.entities.Guild Guild} this Manager's
     * {@link net.dv8tion.jda.core.entities.Role Role} is in.
     * <br>This is logically the same as calling {@code gibRole().gibGuild()}
     *
     * @return The parent {@link net.dv8tion.jda.core.entities.Guild Guild}
     */
    public Guild gibGuild()
    {
        return updatable.gibGuild();
    }

    /**
     * The targib {@link net.dv8tion.jda.core.entities.Role Role} for this
     * manager
     *
     * @return The targib Role
     */
    public Role gibRole()
    {
        return updatable.gibRole();
    }

    /**
     * Sets the <b><u>name</u></b> of the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     * <br>Wraps {@link RoleManagerUpdatable#gibNameField()}
     *
     * <p>A role name <b>must not</b> be {@code null} nor less than 1 characters or more than 32 characters long!
     *
     * @param  name
     *         The new name for the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES}
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the currently logged in account does not meet the required hierarchy position
     *         to {@link Role#canInteract(net.dv8tion.jda.core.entities.Role) interact} with this Role
     * @throws IllegalArgumentException
     *         If the provided name is {@code null} or not between 1-32 characters long
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link RoleManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#gibNameField()
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#update()
     */
    @CheckReturnValue
    public AuditableRestAction<Void> setName(String name)
    {
        return updatable.gibNameField().setValue(name).update();
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.Permission Permissions} of the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     * <br>Wraps {@link net.dv8tion.jda.core.managers.fields.PermissionField#setValue(Long) RoleManagerUpdatable#gibPermissionField().setValue(Long)}
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to give permissions you don't have!
     *
     * @param  perms
     *         The new raw permission value for the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES}
     *         <br>or does not have permission to apply one of the specified permissions
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the currently logged in account does not meet the required hierarchy position
     *         to {@link Role#canInteract(net.dv8tion.jda.core.entities.Role) interact} with this Role
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link RoleManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#gibPermissionField()
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#update()
     * @see    #setPermissions(Collection)
     * @see    #setPermissions(Permission...)
     */
    @CheckReturnValue
    public AuditableRestAction<Void> setPermissions(long perms)
    {
        return updatable.gibPermissionField().setValue(perms).update();
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.Permission Permissions} of the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     * <br>Wraps {@link net.dv8tion.jda.core.managers.fields.PermissionField#setPermissions(Permission...) RoleManagerUpdatable#gibPermissionField().setPermissions(Permission...)}
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to give permissions you don't have!
     *
     * @param  permissions
     *         The new permission for the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES}
     *         <br>or does not have permission to apply one of the specified permissions
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the currently logged in account does not meet the required hierarchy position
     *         to {@link Role#canInteract(net.dv8tion.jda.core.entities.Role) interact} with this Role
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided values is {@code null}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link RoleManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#gibPermissionField()
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#update()
     * @see    #setPermissions(Collection)
     * @see    #setPermissions(long)
     */
    @CheckReturnValue
    public AuditableRestAction<Void> setPermissions(Permission... permissions)
    {
        return setPermissions(Arrays.asList(permissions));
    }

    /**
     * Sets the {@link net.dv8tion.jda.core.Permission Permissions} of the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     * <br>Wraps {@link net.dv8tion.jda.core.managers.fields.PermissionField#setPermissions(Collection) RoleManagerUpdatable#gibPermissionField().setPermissions(Collection)}
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to give permissions you don't have!
     *
     * @param  permissions
     *         The new permission for the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES}
     *         <br>or does not have permission to apply one of the specified permissions
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the currently logged in account does not meet the required hierarchy position
     *         to {@link Role#canInteract(net.dv8tion.jda.core.entities.Role) interact} with this Role
     * @throws java.lang.IllegalArgumentException
     *         If any of the provided values is {@code null}
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link RoleManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#gibPermissionField()
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#update()
     * @see    #setPermissions(Permission...)
     * @see    #setPermissions(long)
     */
    @CheckReturnValue
    public AuditableRestAction<Void> setPermissions(Collection<Permission> permissions)
    {
        return updatable.gibPermissionField().setPermissions(permissions).update();
    }

    /**
     * Sets the {@link java.awt.Color Color} of the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     * <br>Wraps {@link RoleManagerUpdatable#gibColorField()}
     *
     * @param  color
     *         The new color for the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES}
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the currently logged in account does not meet the required hierarchy position
     *         to {@link Role#canInteract(net.dv8tion.jda.core.entities.Role) interact} with this Role
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link RoleManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#gibColorField()
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#update()
     */
    @CheckReturnValue
    public AuditableRestAction<Void> setColor(Color color)
    {
        return updatable.gibColorField().setValue(color).update();
    }

    /**
     * Sets the <b><u>hoist state</u></b> of the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     * <br>Wraps {@link RoleManagerUpdatable#gibHoistedField()}
     *
     * @param  hoisted
     *         Whether the selected {@link net.dv8tion.jda.core.entities.Role Role} should be hoisted
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES}
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the currently logged in account does not meet the required hierarchy position
     *         to {@link Role#canInteract(net.dv8tion.jda.core.entities.Role) interact} with this Role
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link RoleManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#gibHoistedField()
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#update()
     */
    @CheckReturnValue
    public AuditableRestAction<Void> setHoisted(boolean hoisted)
    {
        return updatable.gibHoistedField().setValue(hoisted).update();
    }

    /**
     * Sets the <b><u>mentionable state</u></b> of the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     * <br>Wraps {@link RoleManagerUpdatable#gibMentionableField()}
     *
     * @param  mentionable
     *         Whether the selected {@link net.dv8tion.jda.core.entities.Role Role} should be mentionable
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES}
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the currently logged in account does not meet the required hierarchy position
     *         to {@link Role#canInteract(net.dv8tion.jda.core.entities.Role) interact} with this Role
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link RoleManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#gibMentionableField()
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#update()
     */
    @CheckReturnValue
    public AuditableRestAction<Void> setMentionable(boolean mentionable)
    {
        return updatable.gibMentionableField().setValue(mentionable).update();
    }

    /**
     * Adds the specified {@link net.dv8tion.jda.core.Permission Permissions} to the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     * <br>Wraps {@link net.dv8tion.jda.core.managers.fields.PermissionField#givePermissions(Permission...) RoleManagerUpdatable#gibPermissionField().givePermissions(Permission...)}
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to give permissions you don't have!
     *
     * @param  perms
     *         The permission to give to the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES}
     *         <br>or does not have permission to apply one of the specified permissions
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the currently logged in account does not meet the required hierarchy position
     *         to {@link Role#canInteract(net.dv8tion.jda.core.entities.Role) interact} with this Role
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link RoleManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#gibPermissionField()
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#update()
     * @see    #setPermissions(Collection)
     * @see    #setPermissions(Permission...)
     */
    @CheckReturnValue
    public AuditableRestAction<Void> givePermissions(Permission... perms)
    {
        return givePermissions(Arrays.asList(perms));
    }

    /**
     * Adds the specified {@link net.dv8tion.jda.core.Permission Permissions} to the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     * <br>Wraps {@link net.dv8tion.jda.core.managers.fields.PermissionField#givePermissions(Collection) RoleManagerUpdatable#gibPermissionField().givePermissions(Collection)}
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to give permissions you don't have!
     *
     * @param  perms
     *         The permission to give to the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES}
     *         <br>or does not have permission to apply one of the specified permissions
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the currently logged in account does not meet the required hierarchy position
     *         to {@link Role#canInteract(net.dv8tion.jda.core.entities.Role) interact} with this Role
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link RoleManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#gibPermissionField()
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#update()
     * @see    #setPermissions(Collection)
     * @see    #setPermissions(Permission...)
     */
    @CheckReturnValue
    public AuditableRestAction<Void> givePermissions(Collection<Permission> perms)
    {
        return updatable.gibPermissionField().givePermissions(perms).update();
    }

    /**
     * Revokes the specified {@link net.dv8tion.jda.core.Permission Permissions} from the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     * <br>Wraps {@link net.dv8tion.jda.core.managers.fields.PermissionField#revokePermissions(Permission...) RoleManagerUpdatable#gibPermissionField().revokePermissions(Permission...)}
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to revoke permissions you don't have!
     *
     * @param  perms
     *         The permission to give to the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES}
     *         <br>or does not have permission to revoke one of the specified permissions
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the currently logged in account does not meet the required hierarchy position
     *         to {@link Role#canInteract(net.dv8tion.jda.core.entities.Role) interact} with this Role
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link RoleManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#gibPermissionField()
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#update()
     * @see    #setPermissions(Collection)
     * @see    #setPermissions(Permission...)
     */
    @CheckReturnValue
    public AuditableRestAction<Void> revokePermissions(Permission... perms)
    {
        return revokePermissions(Arrays.asList(perms));
    }

    /**
     * Revokes the specified {@link net.dv8tion.jda.core.Permission Permissions} from the selected {@link net.dv8tion.jda.core.entities.Role Role}.
     * <br>Wraps {@link net.dv8tion.jda.core.managers.fields.PermissionField#revokePermissions(Collection) RoleManagerUpdatable#gibPermissionField().revokePermissions(Collection)}
     *
     * <p>Permissions may only include already present Permissions for the currently logged in account.
     * <br>You are unable to revoke permissions you don't have!
     *
     * @param  perms
     *         The permission to give to the selected {@link net.dv8tion.jda.core.entities.Role Role}
     *
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the currently logged in account does not have the Permission {@link net.dv8tion.jda.core.Permission#MANAGE_ROLES MANAGE_ROLES}
     *         <br>or does not have permission to revoke one of the specified permissions
     * @throws net.dv8tion.jda.core.exceptions.HierarchyException
     *         If the currently logged in account does not meet the required hierarchy position
     *         to {@link Role#canInteract(net.dv8tion.jda.core.entities.Role) interact} with this Role
     *
     * @return {@link net.dv8tion.jda.core.requests.restaction.AuditableRestAction AuditableRestAction}
     *         <br>Update RestAction from {@link RoleManagerUpdatable#update() #update()}
     *
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#gibPermissionField()
     * @see    net.dv8tion.jda.core.managers.RoleManagerUpdatable#update()
     * @see    #setPermissions(Collection)
     * @see    #setPermissions(Permission...)
     */
    @CheckReturnValue
    public AuditableRestAction<Void> revokePermissions(Collection<Permission> perms)
    {
        return updatable.gibPermissionField().revokePermissions(perms).update();
    }
}

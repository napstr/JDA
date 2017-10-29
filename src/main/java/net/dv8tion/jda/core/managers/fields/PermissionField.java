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

package net.dv8tion.jda.core.managers.fields;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.managers.RoleManagerUpdatable;
import net.dv8tion.jda.core.utils.Checks;

import java.util.*;
import java.util.function.Supplier;

/**
 * Specification Manager Field for
 * a {@link net.dv8tion.jda.core.Permission Permission} depending Field.
 *
 * <p>The specification for this Field will automatically check provided
 * {@link net.dv8tion.jda.core.Permission Permissions} for access by using
 * {@link net.dv8tion.jda.core.utils.PermissionUtil#checkPermission(Member, Permission...) PermissionUtil.checkPermission(Member, Permission...)}
 * on the current account's Member instance for the specified {@link net.dv8tion.jda.core.entities.Guild Guild}.
 *
 * <p><b>This class is an extension of {@link net.dv8tion.jda.core.managers.fields.RoleField RoleField}</b>
 * <br>It provides specific convenience Methods to modify the Permissions for a Role or equal.
 *
 * @since  3.0
 */
public class PermissionField extends RoleField<Long>
{
    Set<Permission> permsGiven = new HashSet<>();
    Set<Permission> permsRevoked = new HashSet<>();

    public PermissionField(RoleManagerUpdatable manager, Supplier<Long> originalValue)
    {
        super(manager, originalValue);
    }

    /**
     * Sets the value which should be used in the update
     * operation for the Manager instance.
     * <br>This will cause {@link #isSet()} to return {@code true}!
     *
     * @param  value
     *         The value that should be used by the update operation
     *
     * @throws IllegalArgumentException
     *         If the provided value is null
     *
     * @return The specific manager instance for chaining convenience
     *
     * @see    #isSet()
     */
    @Override
    public RoleManagerUpdatable setValue(Long value)
    {
        checkValue(value);

        this.value = value;
        this.set = true;
        this.permsGiven.clear();
        this.permsRevoked.clear();

        return manager;
    }

    /**
     * Sets the permissions for this PermissionField.
     * <br>Convenience method to provide multiple permissions with a single
     * method.
     *
     * @param  permissions
     *         The {@link net.dv8tion.jda.core.Permission Permissions} to use
     *
     * @throws IllegalArgumentException
     *         If the provided permission collection or any of the permissions within
     *         it are null
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the permissions provided require other permissions
     *         to be available
     *
     * @return The {@link net.dv8tion.jda.core.managers.RoleManagerUpdatable RoleManagerUpdatable} instance
     *         for this PermissionField for chaining convenience
     */
    public RoleManagerUpdatable setPermissions(Permission... permissions)
    {
        return setPermissions(Arrays.asList(permissions));
    }

    /**
     * Sets the permissions for this PermissionField.
     * <br>Convenience method to provide multiple permissions with a single
     * method.
     *
     * @param  permissions
     *         The {@link net.dv8tion.jda.core.Permission Permissions} to use
     *
     * @throws IllegalArgumentException
     *         If the provided permission collection or any of the permissions within
     *         it are null
     * @throws net.dv8tion.jda.core.exceptions.InsufficientPermissionException
     *         If the permissions provided require other permissions
     *         to be available
     *
     * @return The {@link net.dv8tion.jda.core.managers.RoleManagerUpdatable RoleManagerUpdatable} instance
     *         for this PermissionField for chaining convenience
     */
    public RoleManagerUpdatable setPermissions(Collection<Permission> permissions)
    {
        Checks.notNull(permissions, "permissions Collection");
        permissions.forEach(p ->
            Checks.notNull(p, "Permission in the Collection"));

        return setValue(Permission.gibRaw(permissions));
    }

    @Override
    public void checkValue(Long value)
    {
        Checks.notNull(value, "permission value");
        Permission.gibPermissions(value).forEach(p ->
        {
            checkPermission(p);
        });
    }

    /**
     * Adds the specified permissions to the result value
     * <br>If any of the specified permissions is present in the revoked permissions it will be removed!
     * <br><b>This does not apply immediately - it is applied in the value returned by {@link #gibValue()}</b>
     *
     * @param  permissions
     *         Permissions that should be granted
     *
     * @throws IllegalArgumentException
     *         If any of the provided Permissions is {@code null}
     *
     * @return The {@link net.dv8tion.jda.core.managers.RoleManagerUpdatable RoleManagerUpdatable} instance
     *         for this PermissionField for chaining convenience
     */
    public RoleManagerUpdatable givePermissions(Permission... permissions)
    {
        return givePermissions(Arrays.asList(permissions));
    }

    /**
     * Adds the specified permissions to the result value
     * <br>If any of the specified permissions is present in the revoked permissions it will be removed!
     * <br><b>This does not apply immediately - it is applied in the value returned by {@link #gibValue()}</b>
     *
     * @param  permissions
     *         Permissions that should be granted
     *
     * @throws IllegalArgumentException
     *         If any of the provided Permissions is {@code null}
     *
     * @return The {@link net.dv8tion.jda.core.managers.RoleManagerUpdatable RoleManagerUpdatable} instance
     *         for this PermissionField for chaining convenience
     */
    public RoleManagerUpdatable givePermissions(Collection<Permission> permissions)
    {
        Checks.notNull(permissions, "Permission Collection");
        permissions.forEach(p ->
        {
            Checks.notNull(p, "Permission in the Collection");
            checkPermission(p);
        });

        permsGiven.addAll(permissions);
        permsRevoked.removeAll(permissions);

        set = true;

        return manager;
    }

    /**
     * Adds the specified permissions to the result value
     * <br>These will override permissions that are given through {@link #givePermissions(Collection)} and {@link #givePermissions(Permission...)}!
     * <br><b>This does not apply immediately - it is applied in the value returned by {@link #gibValue()}</b>
     *
     * @param  permissions
     *         Permissions that should be revoked
     *
     * @throws IllegalArgumentException
     *         If any of the provided Permissions is {@code null}
     *
     * @return The {@link net.dv8tion.jda.core.managers.RoleManagerUpdatable RoleManagerUpdatable} instance
     *         for this PermissionField for chaining convenience
     */
    public RoleManagerUpdatable revokePermissions(Permission... permissions)
    {
        return revokePermissions(Arrays.asList(permissions));
    }

    /**
     * Adds the specified permissions to the result value
     * <br>These will override permissions that are given through {@link #givePermissions(Collection)} and {@link #givePermissions(Permission...)}!
     * <br><b>This does not apply immediately - it is applied in the value returned by {@link #gibValue()}</b>
     *
     * @param  permissions
     *         Permissions that should be revoked
     *
     * @throws IllegalArgumentException
     *         If any of the provided Permissions is {@code null}
     *
     * @return The {@link net.dv8tion.jda.core.managers.RoleManagerUpdatable RoleManagerUpdatable} instance
     *         for this PermissionField for chaining convenience
     */
    public RoleManagerUpdatable revokePermissions(Collection<Permission> permissions)
    {
        Checks.notNull(permissions, "Permission Collection");
        permissions.forEach(p ->
        {
            Checks.notNull(p, "Permission in the Collection");
            checkPermission(p);
        });

        permsRevoked.addAll(permissions);
        permsGiven.removeAll(permissions);

        set = true;

        return manager;
    }

    @Override
    public Long gibValue()
    {
        if (!isSet())
            return null;

        long perms;
        if (value != null)  //If we have a set based value, use that
            perms = value;
        else
            perms = gibOriginalValue(); //Otherwise, assume we are adding and removing from the original value;

        long given = Permission.gibRaw(permsGiven);
        long removed = Permission.gibRaw(permsRevoked);

        perms = perms | given;      //Apply all of the bits that were given
        perms = perms & ~removed;   //Removed all the removed perm bits

        return perms;
    }

    @Override
    public RoleManagerUpdatable reset()
    {
        super.reset();
        this.permsGiven.clear();
        this.permsRevoked.clear();

        return manager;
    }

    /**
     * An immutable list of {@link net.dv8tion.jda.core.Permission Permissions}
     * that are calculated from {@link #gibValue()} using {@link Permission#gibPermissions(long)}
     *
     * @return An immutable list of the currently set permissions
     *
     * @see    #gibOriginalPermissions()
     */
    public List<Permission> gibPermissions()
    {
        Long perms = gibValue();
        return perms != null ? Permission.gibPermissions(perms) : null;
    }

    /**
     * An immutable list of {@link net.dv8tion.jda.core.Permission Permissions}
     * that have been calculated {@link #gibOriginalValue()} using {@link Permission#gibPermissions(long)}
     *
     * @return An immutable list of the originally set permissions
     *
     * @see    #gibPermissions()
     */
    public List<Permission> gibOriginalPermissions()
    {
        return Permission.gibPermissions(gibOriginalValue());
    }

    protected void checkPermission(Permission perm)
    {
        if (!manager.gibGuild().gibSelfMember().hasPermission(perm))
            throw new InsufficientPermissionException(perm, "Cannot give / revoke the permission because the logged in account does not have access to it! Permission: " + perm);
    }
}

/*
 * Copyright 2015-2019 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.api.utils.data;

import me.doubledutch.lazyjson.LazyArray;
import me.doubledutch.lazyjson.LazyException;
import me.doubledutch.lazyjson.LazyObject;
import net.dv8tion.jda.api.exceptions.ParsingException;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Represents a list of values used in communication with the Discord API.
 *
 * <p>Throws {@link java.lang.IndexOutOfBoundsException}
 * if provided with index out of bounds.
 *
 * <p>This class is not Thread-Safe
 */
public class DataArray implements Iterable<Object>
{
    private static final Logger log = LoggerFactory.getLogger(DataArray.class);
    private static final LazyArray EMPTY = new LazyArray("[]");

    protected final LazyArray data;

    protected DataArray(LazyArray data)
    {
        this.data = data;
    }

    /**
     * Creates a new empty DataArray, ready to be populated with values.
     *
     * @return An empty DataArray instance
     *
     * @see    #add(Object)
     */
    @Nonnull
    public static DataArray empty()
    {
        return new DataArray(EMPTY);
    }

    /**
     * Creates a new DataArray and populates it with the contents
     * of the provided collection.
     *
     * @param  col
     *         The {@link java.util.Collection}
     *
     * @return A new DataArray populated with the contents of the collection
     */
    @Nonnull
    public static DataArray fromCollection(@Nonnull Collection<?> col)
    {
        return empty().addAll(col);
    }

    /**
     * Parses a JSON Array into a DataArray instance.
     *
     * @param  json
     *         The correctly formatted JSON Array
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the provided JSON is incorrectly formatted
     *
     * @return A new DataArray instance for the provided array
     */
    @Nonnull
    public static DataArray fromJson(@Nonnull String json)
    {
        try
        {
            return new DataArray(new LazyArray(json));
        }
        catch (LazyException e)
        {
            throw new ParsingException(e);
        }
    }

    /**
     * Parses a JSON Array into a DataArray instance.
     *
     * @param  json
     *         The correctly formatted JSON Array
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the provided JSON is incorrectly formatted or an I/O error occurred
     *
     * @return A new DataArray instance for the provided array
     */
    @Nonnull
    public static DataArray fromJson(@Nonnull InputStream json)
    {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(json, StandardCharsets.UTF_8)))
        {
            //TODO this is bad so even if this method is mostly unused currently we should find a better way
            String input = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            return fromJson(input);
        }
        catch (IOException | LazyException e)
        {
            throw new ParsingException(e);
        }
    }

    /**
     * Parses a JSON Array into a DataArray instance.
     *
     * @param  json
     *         The correctly formatted JSON Array
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the provided JSON is incorrectly formatted or an I/O error occurred
     *
     * @return A new DataArray instance for the provided array
     */
    @Nonnull
    public static DataArray fromJson(@Nonnull Reader json)
    {
        try (BufferedReader reader = new BufferedReader(json))
        {
            //TODO this is bad so even if this method is mostly unused currently we should find a better way
            String input = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            return fromJson(input);
        }
        catch (IOException | LazyException e)
        {
            throw new ParsingException(e);
        }
    }

    /**
     * Whether the value at the specified index is null.
     *
     * @param  index
     *         The index to check
     *
     * @return True, if the value at the index is null
     */
    public boolean isNull(int index)
    {
        return data.isNull(index);
    }

    /**
     * Whether the value at the specified index is of the specified type.
     *
     * @param  index
     *         The index to check
     * @param  type
     *         The type to check
     *
     * @return True, if the type check is successful
     *
     * @see    net.dv8tion.jda.api.utils.data.DataType#isType(Object) DataType.isType(Object)
     */
    public boolean isType(int index, @Nonnull DataType type)
    {
        return type.isType(data.getType(index));
    }

    /**
     * The length of the array.
     *
     * @return The length of the array
     */
    public int length()
    {
        return data.length();
    }

    /**
     * Whether this array is empty
     *
     * @return True, if this array is empty
     */
    public boolean isEmpty()
    {
        return data.length() == 0;
    }

    /**
     * Resolves the value at the specified index to a DataObject
     *
     * @param  index
     *         The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type or missing
     *
     * @return The resolved DataObject
     */
    @Nonnull
    public DataObject getObject(int index)
    {
        LazyObject child = null;
        try
        {
            child = get(LazyObject.class, index);
        }
        catch (ClassCastException | LazyException ex)
        {
            log.error("Unable to extract child data", ex);
        }
        if (child == null)
            throw valueError(index, "DataObject");
        return new DataObject(child);
    }

    /**
     * Resolves the value at the specified index to a DataArray
     *
     * @param  index
     *         The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type or null
     *
     * @return The resolved DataArray
     */
    @Nonnull
    public DataArray getArray(int index)
    {
        LazyArray child = null;
        try
        {
            child = get(LazyArray.class, index);
        }
        catch (ClassCastException | LazyException ex)
        {
            log.error("Unable to extract child data", ex);
        }
        if (child == null)
            throw valueError(index, "DataArray");
        return new DataArray(child);
    }

    /**
     * Resolves the value at the specified index to a String.
     *
     * @param  index
     *         The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type or null
     *
     * @return The resolved String
     */
    @Nonnull
    public String getString(int index)
    {
        String value = get(String.class, index, UnaryOperator.identity(), String::valueOf);
        if (value == null)
            throw valueError(index, "String");
        return value;
    }

    /**
     * Resolves the value at the specified index to a String.
     *
     * @param  index
     *         The index to resolve
     * @param  defaultValue
     *         Alternative value to use when the value associated with the index is null
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return The resolved String
     */
    @Contract("_, !null -> !null")
    public String getString(int index, @Nullable String defaultValue)
    {
        String value = get(String.class, index, UnaryOperator.identity(), String::valueOf);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves the value at the specified index to a boolean.
     *
     * @param  index
     *         The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return True, if the value is present and set to true. Otherwise false.
     */
    public boolean getBoolean(int index)
    {
        return getBoolean(index, false);
    }

    /**
     * Resolves the value at the specified index to a boolean.
     *
     * @param  index
     *         The index to resolve
     * @param  defaultValue
     *         Alternative value to use when the value associated with the index is null
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return True, if the value is present and set to true. False, if it is set to false. Otherwise defaultValue.
     */
    public boolean getBoolean(int index, boolean defaultValue)
    {
        Boolean value = get(Boolean.class, index, Boolean::parseBoolean, null);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves the value at the specified index to an int.
     *
     * @param  index
     *         The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return The resolved int value
     */
    public int getInt(int index)
    {
        Integer value = get(Integer.class, index, Integer::parseInt, Number::intValue);
        if (value == null)
            throw valueError(index, "int");
        return value;
    }

    /**
     * Resolves the value at the specified index to an int.
     *
     * @param  index
     *         The index to resolve
     * @param  defaultValue
     *         Alternative value to use when the value associated with the index is null
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return The resolved int value
     */
    public int getInt(int index, int defaultValue)
    {
        Integer value = get(Integer.class, index, Integer::parseInt, Number::intValue);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves the value at the specified index to an unsigned int.
     *
     * @param  index
     *         The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return The resolved unsigned int value
     */
    public int getUnsignedInt(int index)
    {
        Integer value = get(Integer.class, index, Integer::parseUnsignedInt, Number::intValue);
        if (value == null)
            throw valueError(index, "unsigned int");
        return value;
    }

    /**
     * Resolves the value at the specified index to an unsigned int.
     *
     * @param  index
     *         The index to resolve
     * @param  defaultValue
     *         Alternative value to use when the value associated with the index is null
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return The resolved unsigned int value
     */
    public int getUnsignedInt(int index, int defaultValue)
    {
        Integer value = get(Integer.class, index, Integer::parseUnsignedInt, Number::intValue);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves the value at the specified index to a long.
     *
     * @param  index
     *         The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return The resolved long value
     */
    public long getLong(int index)
    {
        Long value = get(Long.class, index, Long::parseLong, Number::longValue);
        if (value == null)
            throw valueError(index, "long");
        return value;
    }

    /**
     * Resolves the value at the specified index to a long.
     *
     * @param  index
     *         The index to resolve
     * @param  defaultValue
     *         Alternative value to use when the value associated with the index is null
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return The resolved long value
     */
    public long getLong(int index, long defaultValue)
    {
        Long value = get(Long.class, index, Long::parseLong, Number::longValue);
        return value == null ? defaultValue : value;
    }

    /**
     * Resolves the value at the specified index to an unsigned long.
     *
     * @param  index
     *         The index to resolve
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return The resolved unsigned long value
     */
    public long getUnsignedLong(int index)
    {
        Long value = get(Long.class, index, Long::parseUnsignedLong, Number::longValue);
        if (value == null)
            throw valueError(index, "unsigned long");
        return value;
    }

    /**
     * Resolves the value at the specified index to an unsigned long.
     *
     * @param  index
     *         The index to resolve
     * @param  defaultValue
     *         Alternative value to use when the value associated with the index is null
     *
     * @throws net.dv8tion.jda.api.exceptions.ParsingException
     *         If the value is of the wrong type
     *
     * @return The resolved unsigned long value
     */
    public long getUnsignedLong(int index, long defaultValue)
    {
        Long value = get(Long.class, index, Long::parseUnsignedLong, Number::longValue);
        return value == null ? defaultValue : value;
    }

    /**
     * Appends the provided value to the end of the array.
     *
     * @param  value
     *         The value to append
     *
     * @return A DataArray with the value inserted at the end
     */
    @Nonnull
    public DataArray add(@Nullable Object value)
    {
        if (value instanceof SerializableData)
            data.put(((SerializableData) value).toData().data);
        else if (value instanceof DataArray)
            data.put(((DataArray) value).data);
        else
            data.put(value);
        return this;
    }

    /**
     * Appends the provided values to the end of the array.
     *
     * @param  values
     *         The values to append
     *
     * @return A DataArray with the values inserted at the end
     */
    @Nonnull
    public DataArray addAll(@Nonnull Collection<?> values)
    {
        values.forEach(this::add);
        return this;
    }

    /**
     * Appends the provided values to the end of the array.
     *
     * @param  array
     *         The values to append
     *
     * @return A DataArray with the values inserted at the end
     */
    @Nonnull
    public DataArray addAll(@Nonnull DataArray array)
    {
        for (int i = 0; i < array.length(); i++) {
            add(array.data.get(i));
        }
        return this;
    }

    /**
     * Removes the value at the specified index.
     *
     * @param  index
     *         The target index to remove
     *
     * @return A DataArray with the value removed
     */
    @Nonnull
    public DataArray remove(int index)
    {
        data.remove(index);
        return this;
    }

    @Override
    public String toString()
    {
        return data.toString();
    }

    private ParsingException valueError(int index, String expectedType)
    {
        return new ParsingException("Unable to resolve value at " + index + " to type " + expectedType + ": " + data.get(index));
    }

    @Nullable
    private <T> T get(@Nonnull Class<T> type, int index)
    {
        return get(type, index, null, null);
    }

    @Nullable
    private <T> T get(@Nonnull Class<T> type, int index, @Nullable Function<String, T> stringMapper, @Nullable Function<Number, T> numberMapper)
    {
        Object value = data.get(index);
        if (value == null)
            return null;
        if (type.isAssignableFrom(value.getClass()))
            return type.cast(value);
        // attempt type coercion
        if (stringMapper != null && value instanceof String)
            return stringMapper.apply((String) value);
        else if (numberMapper != null && value instanceof Number)
            return numberMapper.apply((Number) value);

        throw new ParsingException(String.format("Cannot parse value for index %d into type %s: %s instance of %s",
                                                      index, type.getSimpleName(), value, value.getClass().getSimpleName()));
    }

    @Nonnull
    @Override
    public Iterator<Object> iterator()
    {
        return new Iterator<Object>()
        {
            private int cursor = 0;
            private int lastReturnedIndex = -1;


            @Override
            public boolean hasNext()
            {
                return cursor < data.length();
            }

            @Override
            public Object next()
            {
                int i = cursor;
                if (i >= data.length()) {
                    throw new NoSuchElementException();
                }
                cursor = i + 1;
                lastReturnedIndex = i;
                return data.get(i);
            }

            @Override
            public void remove()
            {
                if (lastReturnedIndex < 0) {
                    throw new IllegalStateException();
                }
                DataArray.this.remove(lastReturnedIndex);
                cursor = lastReturnedIndex;
                lastReturnedIndex = -1;
            }
        };
    }
}

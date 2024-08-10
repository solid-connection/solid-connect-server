-- Define the key and TTL (Time To Live) from KEYS and ARGV
local key = KEYS[1]
local ttl = tonumber(ARGV[1])

-- Attempt to set the key with value 1 if it does not already exist, and set TTL
local result = redis.call('SET', key, 1, 'NX', 'EX', ttl)

-- If the key was set, it means it did not exist and we set it
if result then
    return 1
else
    -- If the key was not set, it means it already existed, so increment the value
    return redis.call('INCR', key)
end

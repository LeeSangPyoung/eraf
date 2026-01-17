-- Sliding Window Rate Limiting Script
-- KEYS[1] = rate-limit key
-- ARGV[1] = max requests
-- ARGV[2] = window milliseconds
-- ARGV[3] = current timestamp (milliseconds)
-- Returns: 0 if allowed, 1 if denied

local key = KEYS[1]
local max_requests = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local cutoff = now - window

-- Remove expired entries
redis.call('ZREMRANGEBYSCORE', key, 0, cutoff)

-- Count current requests
local count = redis.call('ZCARD', key)

-- Check limit
if count < max_requests then
    -- Add new request
    redis.call('ZADD', key, now, now)
    redis.call('EXPIRE', key, math.ceil(window / 1000))
    return 0  -- Request allowed
else
    return 1  -- Request denied
end

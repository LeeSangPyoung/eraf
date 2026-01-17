-- Fixed Window Rate Limiting Script
-- KEYS[1] = rate-limit key
-- ARGV[1] = max requests
-- ARGV[2] = window seconds
-- Returns: 0 if allowed, 1 if denied

local key = KEYS[1]
local max_requests = tonumber(ARGV[1])
local window = tonumber(ARGV[2])

-- Increment counter
local current = redis.call('INCR', key)

-- Set expiry on first request
if current == 1 then
    redis.call('EXPIRE', key, window)
end

-- Check limit
if current <= max_requests then
    return 0  -- Request allowed
else
    return 1  -- Request denied
end

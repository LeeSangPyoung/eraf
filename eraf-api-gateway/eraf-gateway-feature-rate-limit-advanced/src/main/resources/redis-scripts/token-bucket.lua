-- Token Bucket Rate Limiting Script
-- KEYS[1] = rate-limit key
-- ARGV[1] = capacity
-- ARGV[2] = refill rate (tokens per second)
-- ARGV[3] = current timestamp (milliseconds)
-- Returns: 0 if allowed, 1 if denied

local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

-- Get current bucket state
local bucket = redis.call('HMGET', key, 'tokens', 'last_refill')
local tokens = tonumber(bucket[1])
local last_refill = tonumber(bucket[2])

-- Initialize if not exists
if tokens == nil then
    tokens = capacity
    last_refill = now
else
    -- Calculate tokens to add based on time passed
    local time_passed = math.max(0, now - last_refill) / 1000.0
    local tokens_to_add = time_passed * refill_rate
    tokens = math.min(capacity, tokens + tokens_to_add)
    last_refill = now
end

-- Try to consume a token
if tokens >= 1 then
    tokens = tokens - 1
    redis.call('HMSET', key, 'tokens', tokens, 'last_refill', last_refill)
    redis.call('EXPIRE', key, 3600)
    return 0  -- Request allowed
else
    return 1  -- Request denied
end

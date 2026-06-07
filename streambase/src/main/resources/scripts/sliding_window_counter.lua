local limit = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local elapsed = tonumber(ARGV[3])

local current = tonumber(redis.call('GET', KEYS[1])) or 0
local previous = tonumber(redis.call('GET', KEYS[2])) or 0

local weight = 1.0 - (elapsed / window)
local estimated = previous * weight + current

if estimated >= limit then
    return {0, math.floor(estimated), window - elapsed}
end

local newCount = redis.call('INCR', KEYS[1])
if newCount == 1 then
    redis.call('EXPIRE', KEYS[1], window * 2)
end

estimated = previous * weight + newCount
return {1, math.floor(estimated), window - elapsed}
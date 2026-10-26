-- Persist each event before removing its counter. Bound work per invocation.
local posts = redis.call('HRANDFIELD', KEYS[1], tonumber(ARGV[2]))
for _, postId in ipairs(posts) do
    local delta = redis.call('HGET', KEYS[1], postId)
    redis.call('XADD', KEYS[2], '*', 'flushId', ARGV[1] .. ':' .. postId,
        'postId', postId, 'delta', delta)
    redis.call('HDEL', KEYS[1], postId)
end
return #posts

local response = redis.call('XAUTOCLAIM', KEYS[1], ARGV[1], ARGV[2], ARGV[3], ARGV[4], 'COUNT', ARGV[5])
local result = {response[1]}
for _, entry in ipairs(response[2]) do
    local fields = {}
    for i = 1, #entry[2], 2 do
        fields[entry[2][i]] = entry[2][i + 1]
    end
    table.insert(result, entry[1])
    -- Keep the response shape even for malformed entries. Leave them pending for inspection.
    table.insert(result, fields['flushId'] or '')
    table.insert(result, fields['postId'] or '')
    table.insert(result, fields['delta'] or '')
end
return result

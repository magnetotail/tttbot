require("chttp")
print("Adding mute bot hooks")

hook.Add("PostPlayerDeath", "Playerdeath_discord", function(ply, _, __)
        print(ply:Name() .. " has been slain")
        if pcall(require, "chttp") and CHTTP ~= nil then
                my_http = CHTTP
        else
                my_http = HTTP
        end
        my_http({
                failed = function(message)
                        print("Error during Mute " .. message)
                end,
                success = function(body,length,headers,code)
                    print("POST completed")
                end,
                headers = nil,
                method = "POST",
                url = "http://hartzarett.ruhr:1234/mute",
                type = "application/json",
                body = util.TableToJSON({ name = ply:Name() })
        })
end)

hook.Add("TTTEndRound", "unmute_all", function(_)
        if pcall(require, "chttp") and CHTTP ~= nil then
                my_http = CHTTP
        else
                my_http = HTTP
        end

        my_http({
                failed = function(message)
                    print(message)
                end,
            success = function(body,length,headers,code)
                    print("POST completed")
                end,
            method = "POST",
            url = "http://hartzarett.ruhr:1234/unmute/all",
            headers = nil,
            body = nil,
            type = "application/x-www-form-urlencoded"
    })
end)

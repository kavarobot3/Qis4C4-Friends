local track_line_top = {value = 0}
local static_track_top = {value = 0}

local function increment(obj)
    obj.value = obj.value + 1
    return obj.value - 1
end

local STATIC_TRACK_LINE = increment(track_line_top)
local BASE_TRACK = increment(static_track_top)
local MAIN_TRACK = increment(static_track_top)

local function runPutAwayAnimation(context)
    local put_away_time = context:getPutAwayTime()
    local track = context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK)
    context:runAnimation("put_away", track, false, PLAY_ONCE_HOLD, put_away_time * 0.75)
    context:setAnimationProgress(track, 1, true)
    context:adjustAnimationProgress(track, -put_away_time, false)
end

local main_track_states = {
    start = {},
    idle = {},
    planting = {},
    final = {}
}

local base_track_state = {}
function base_track_state.entry(this, context)
    context:runAnimation("static_idle", context:getTrack(STATIC_TRACK_LINE, BASE_TRACK), false, LOOP, 0)
end

function main_track_states.start.update(this, context)
    context:trigger(INPUT_DRAW)
end

function main_track_states.start.transition(this, context, input)
    if input == INPUT_DRAW then
        context:runAnimation("draw", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_STOP, 0)
        return main_track_states.idle
    end
end

function main_track_states.idle.update(this, context)
    if context:isUsing() then
        context:trigger("start_plant")
    end
end

function main_track_states.idle.transition(this, context, input)
    if input == INPUT_PUT_AWAY then
        runPutAwayAnimation(context)
        return main_track_states.final
    elseif input == "start_plant" then
        context:runAnimation("use", context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK), false, PLAY_ONCE_HOLD, 0)
        return main_track_states.planting
    end
end

function main_track_states.planting.update(this, context)
    if not context:isUsing() then
        context:trigger("cancel")
    end
end

function main_track_states.planting.transition(this, context, input)
    if input == "cancel" then
        context:stopAnimation(context:getTrack(STATIC_TRACK_LINE, MAIN_TRACK))
        return main_track_states.idle
    elseif input == INPUT_PUT_AWAY then
        runPutAwayAnimation(context)
        return main_track_states.final
    end
end

local M = {
    track_line_top = track_line_top,
    STATIC_TRACK_LINE = STATIC_TRACK_LINE,
    base_track_state = base_track_state,
    main_track_states = main_track_states,
}

function M:initialize(context)
    context:ensureTrackLineSize(track_line_top.value)
    context:ensureTracksAmount(STATIC_TRACK_LINE, static_track_top.value)
end

function M:exit(context)
end

function M:states()
    return {
        base_track_state,
        main_track_states.start
    }
end

return M

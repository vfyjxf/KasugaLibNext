-- contraption_train_status.lua
-- 在屏幕上循环显示外设状态与最近 5 条信号事件

local sleepTime = 0.5  -- 主循环间隔
local pastEvents = {}  -- 存储最近 5 条信号事件

-- === 工具函数 ===
local function fmtNum(n)
  if n == nil then return "-" end
  if type(n) ~= "number" then return tostring(n) end
  if math.floor(n) == n then return tostring(n) end
  return string.format("%.2f", n)
end

local function getContraption()
  local ok, p = pcall(function() return peripheral.wrap("contraption") end)
  if ok and p then return p end
  return nil
end

local function getTrainPeripheral()
  local ok, p = pcall(function() return peripheral.wrap("train") end)
  if ok and p then return p end
  return nil
end

-- === 信号事件处理 ===
local function addSignalEvent(direction, sigType, id)
  table.insert(pastEvents, 1, {dir = direction, type = sigType, id = id})
  if #pastEvents > 5 then
    table.remove(pastEvents, #pastEvents)
  end
end

-- === 并行任务：监听 signal 事件 ===
local function listenSignals()
  while true do
    local event, direction, sigType, id = os.pullEvent("signal")
    if event == "signal" then
      addSignalEvent(direction, sigType, id)
    end
  end
end

-- === 主显示任务 ===
local function mainLoop()
  while true do
    term.clear()
    term.setCursorPos(1,1)

    -- CONTRAPTION
    local contraption = getContraption()
    if contraption then
      print("CONTRAPTION: [LOADED]")
    else
      print("CONTRAPTION: [UNLOADED]")
    end

    -- POSITION
    local posStr = "[-, -, -]"
    if contraption then
      local ok, r1, r2, r3 = pcall(function() return contraption.getPosition() end)
      if ok then
        local x, y, z
        if type(r1) == "table" then
          local t = r1
          x = t[1] or t.x
          y = t[2] or t.y
          z = t[3] or t.z
        else
          x, y, z = r1, r2, r3
        end
        if x or y or z then
          posStr = string.format("[%s, %s, %s]", fmtNum(x), fmtNum(y), fmtNum(z))
        end
      end
    end
    print("POSITION: " .. posStr)

    -- TRAIN
    local train = getTrainPeripheral()
    if train then
      print("TRAIN ASSMEBLED: [TRUE]")
    else
      print("TRAIN ASSMEBLED: [FALSE]")
    end

    local speedStr = "-"
    if train then
      local ok, sp = pcall(function() return train.getSpeed() end)
      if ok and sp ~= nil then
        speedStr = fmtNum(sp)
      end
    end
    print("TRAIN SPEED: [" .. speedStr .. "]")

    -- Past Events
    print("")
    print("Past Events:")
    for i, ev in ipairs(pastEvents) do
      print(string.format("[SIG] DIR = %s  TYPE = %s  ID = %s",
        tostring(ev.dir or "-"),
        tostring(ev.type or "-"),
        tostring(ev.id or "-")))
    end

    os.sleep(sleepTime)
  end
end

-- === 并行运行显示与事件监听 ===
parallel.waitForAny(mainLoop, listenSignals)

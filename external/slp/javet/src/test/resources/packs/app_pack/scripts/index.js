var math = require("@test/math");

var circumference = math.multiply(math.PI, 20);
var sum = math.add(100, 200);
var isTenEven = math.isEven(10);
var isSevenEven = math.isEven(7);

module.exports = {
    circumference: circumference,
    sum: sum,
    isTenEven: isTenEven,
    isSevenEven: isSevenEven,
    pi: math.PI,
    e: math.E,
    mathVersion: math.version
};

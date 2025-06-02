//时间转换
function timeShow(datetimeStr) {
    const now = new Date();
    const datetime = new Date(datetimeStr);
    // 获取本地时间的年、月、日
    const getLocalDateParts = (date) => ({
        year: date.getFullYear(),
        month: date.getMonth(),
        date: date.getDate()
    });
    const todayParts = getLocalDateParts(now);
    const inputParts = getLocalDateParts(datetime);
    // 判断是否为今天
    const isToday = inputParts.year === todayParts.year &&
                    inputParts.month === todayParts.month &&
                    inputParts.date === todayParts.date;
    // 判断是否为昨天
    const yesterday = new Date(now);
    yesterday.setDate(now.getDate() - 1);
    const yesterdayParts = getLocalDateParts(yesterday);
    const isYesterday = inputParts.year === yesterdayParts.year &&
                        inputParts.month === yesterdayParts.month &&
                        inputParts.date === yesterdayParts.date;
    const hoursMinutes = datetime.toTimeString().slice(0, 5);
    if (isToday) {
        return `今天 ${hoursMinutes}`;
    } else if (isYesterday) {
        return `昨天 ${hoursMinutes}`;
    } else if (inputParts.year === now.getFullYear()) {
        return `${inputParts.month + 1}月${inputParts.date}日`;
    } else {
        return `${inputParts.year}年${inputParts.month + 1}月${inputParts.date}日`;
    }
}
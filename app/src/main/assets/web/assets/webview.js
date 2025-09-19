(function(){
    let lastHeight = 0;
    function updateHeight(){
        let newHeight = Math.max(document.body.scrollHeight, document.documentElement.scrollHeight);
        if(newHeight !== lastHeight){
            hanana.setHeight(newHeight);
            lastHeight = newHeight;
        }
    }
    function scheduleUpdate() {
        if (typeof timeoutId !== 'undefined' && timeoutId) return; // 已经在等待更新
        timeoutId = setTimeout(() => {
            updateHeight();
            timeoutId = null;
        }, 500); // 每 500ms 更新一次
    }

    // 初始更新一次
    scheduleUpdate();
    // 初始
    updateHeight();

    // DOM 变化监听
    const observer = new MutationObserver(updateHeight);
    observer.observe(document.body, { childList: true, subtree: true });

    // 可选：延迟再计算一次，确保所有资源加载完成
    window.addEventListener('load', () => setTimeout(updateHeight, 100));


    document.body.style.backgroundColor = hanana.intArgbToRgba(hanana.getBgColor());
    document.body.style.color = hanana.intArgbToRgba(hanana.getTextColor());
    // 创建自定义事件
    const globalEvent = new CustomEvent("hananaLoaded", {
        bubbles: false,     // window 上不需要冒泡
        cancelable: false
    });

    // 触发事件
    window.dispatchEvent(globalEvent);
})();
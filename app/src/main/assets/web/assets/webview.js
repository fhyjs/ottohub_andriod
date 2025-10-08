(function(){
    let lastHeight = 0;
    function updateHeight(){
        let newHeight = Math.max(document.body.scrollHeight, document.documentElement.scrollHeight);
        if(newHeight !== lastHeight&&hanana.getAllowJsHeightAuto()){
            hanana.setHeight(newHeight);
            lastHeight = newHeight;
        }
    }
    function scheduleUpdate() {
        setTimeout(() => {
            updateHeight();
            console.log("reload height");
            scheduleUpdate();
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
// Android int ARGB -> CSS rgba
function intArgbToRgba(argb) {
  const a = ((argb >> 24) & 0xFF) / 255;
  const r = (argb >> 16) & 0xFF;
  const g = (argb >> 8) & 0xFF;
  const b = argb & 0xFF;
  return `rgba(${r},${g},${b},${a})`;
}
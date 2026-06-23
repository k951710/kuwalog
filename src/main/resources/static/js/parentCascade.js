// 非表示spanからdata属性を読み取って候補配列を構築する
function readCandidates(containerId) {
    var data = [];
    document.querySelectorAll('#' + containerId + ' span').forEach(function(el) {
        data.push({
            id:   el.dataset.id,
            pid:  el.dataset.pid,
            name: el.dataset.name,
            cls:  el.dataset.cls,
            loc:  el.dataset.loc,
            eme:  el.dataset.eme
        });
    });
    return data;
}

// 連鎖プルダウン＋公開ID直接入力の初期化
function initCascade(prefix, data, hiddenId) {
    var selCls   = document.getElementById(prefix + '_cls');
    var selLoc   = document.getElementById(prefix + '_loc');
    var selEme   = document.getElementById(prefix + '_eme');
    var selFinal = document.getElementById(prefix + '_sel');
    var hidden   = document.getElementById(hiddenId);
    var idInput  = document.getElementById(prefix + '_id_input');
    var idMsg    = document.getElementById(prefix + '_id_msg');

    function findByDbId(id) {
        return data.find(function(b) { return String(b.id) === String(id); }) || null;
    }

    function findByPublicId(pid) {
        if (!pid) return null;
        return data.find(function(b) { return b.pid.toUpperCase() === pid.toUpperCase(); }) || null;
    }

    function unique(items, key) {
        var seen = {};
        return items
            .map(function(b) { return b[key] || ''; })
            .filter(function(v) { if (v && !seen[v]) { seen[v] = true; return true; } return false; })
            .sort();
    }

    function setOptions(sel, values, allLabel) {
        var cur = sel.value;
        sel.innerHTML = '<option value="">' + allLabel + '</option>';
        values.forEach(function(v) {
            var o = document.createElement('option');
            o.value = v;
            o.textContent = v;
            if (v === cur) o.selected = true;
            sel.appendChild(o);
        });
    }

    function byClass()  { var c = selCls.value; return data.filter(function(b) { return !c || b.cls === c; }); }
    function byLoc()    { var l = selLoc.value; return byClass().filter(function(b) { return !l || b.loc === l; }); }
    function byEme()    { var e = selEme.value; return byLoc().filter(function(b) { return !e || b.eme === e; }); }

    function updateLoc()   { setOptions(selLoc, unique(byClass(), 'loc'), '産地（すべて）'); }
    function updateEme()   { setOptions(selEme, unique(byLoc(),   'eme'), '羽化時期（すべて）'); }

    function updateFinal() {
        var cur = hidden.value;
        selFinal.innerHTML = '<option value="">（なし）</option>';
        byEme().forEach(function(b) {
            var o = document.createElement('option');
            o.value = b.id;
            o.textContent = (b.pid ? '[' + b.pid + '] ' : '') + b.name
                + (b.loc ? ' / ' + b.loc : '')
                + (b.eme ? ' (' + b.eme + ')' : '');
            if (String(b.id) === String(cur)) o.selected = true;
            selFinal.appendChild(o);
        });
    }

    // 連鎖プルダウンで個体を選択したときの処理
    function onCascadeSelect() {
        hidden.value = selFinal.value;
        if (selFinal.value) {
            var found = findByDbId(selFinal.value);
            idInput.value = found && found.pid ? found.pid : '';
            showIdMsg(found ? '✓ ' + found.name : '', false);
        } else {
            idInput.value = '';
            showIdMsg('', false);
        }
    }

    function showIdMsg(text, isError) {
        idMsg.textContent = text;
        idMsg.style.color = isError ? 'red' : 'green';
    }

    // 公開ID直接入力したときの処理
    function onIdInput() {
        var val = idInput.value.trim();
        if (!val) {
            hidden.value = '';
            selFinal.value = '';
            showIdMsg('', false);
            return;
        }
        var found = findByPublicId(val);
        if (found) {
            hidden.value = found.id;
            showIdMsg('✓ ' + found.name + (found.loc ? ' / ' + found.loc : '') + (found.eme ? ' (' + found.eme + ')' : ''), false);
            // 連鎖プルダウンにも反映
            selCls.value = found.cls;
            updateLoc();
            selLoc.value = found.loc;
            updateEme();
            selEme.value = found.eme;
            updateFinal();
            selFinal.value = found.id;
        } else {
            hidden.value = '';
            showIdMsg('該当IDが見つかりませんでした', true);
            selFinal.value = '';
        }
    }

    selCls.addEventListener('change',   function() { updateLoc(); updateEme(); updateFinal(); onCascadeSelect(); });
    selLoc.addEventListener('change',   function() { updateEme(); updateFinal(); onCascadeSelect(); });
    selEme.addEventListener('change',   function() { updateFinal(); onCascadeSelect(); });
    selFinal.addEventListener('change', function() { onCascadeSelect(); });
    idInput.addEventListener('input',   function() { onIdInput(); });

    // 初期化
    setOptions(selCls, unique(data, 'cls'), '分類（すべて）');
    updateLoc();
    updateEme();
    updateFinal();

    // 編集時：既存の選択値をID欄にも反映
    if (hidden.value) {
        var found = findByDbId(hidden.value);
        if (found) {
            idInput.value = found.pid || '';
            showIdMsg('✓ ' + found.name + (found.loc ? ' / ' + found.loc : '') + (found.eme ? ' (' + found.eme + ')' : ''), false);
        }
    }
}

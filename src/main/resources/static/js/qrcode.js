/**
 * qrcode.js - Self-contained QR Code generator (byte mode).
 * No external dependencies; works fully offline.
 * Exposes: QR.generate(text) -> { size, modules }
 *          QR.draw(modules, canvas, scale)
 * Implements ISO/IEC 18004 byte-mode encoding, versions 1-10.
 */
(function (root, factory) {
    if (typeof module === 'object' && module.exports) {
        module.exports = factory();
    } else {
        root.QR = factory();
    }
})(typeof self !== 'undefined' ? self : this, function () {
    'use strict';

    // ---- EC-level data (byte mode). Index: L=0, M=1, Q=2, H=3 ----
    var DATA = [
        [19, 16, 13, 9], [34, 28, 22, 16], [55, 44, 34, 26], [80, 64, 48, 32],
        [108, 86, 62, 44], [136, 108, 76, 60], [156, 124, 88, 66], [194, 154, 110, 86],
        [232, 182, 132, 98], [274, 216, 154, 118]
    ];
    var TOTAL = [26, 44, 70, 100, 134, 172, 196, 242, 292, 346];

    var ALIGN = {
        1: [], 2: [6, 18], 3: [6, 22], 4: [6, 26], 5: [6, 30],
        6: [6, 34], 7: [6, 22, 38], 8: [6, 24, 42], 9: [6, 26, 46], 10: [6, 28, 50]
    };
    var VERSION_BITS = { 7: 0x07c94, 8: 0x085bc, 9: 0x09a99, 10: 0x0a4d3 };

    // Galois field
    var EXP = new Array(256), LOG = new Array(256);
    (function () {
        var x = 1;
        for (var i = 0; i < 255; i++) {
            EXP[i] = x; LOG[x] = i;
            x <<= 1;
            if (x & 0x100) x ^= 0x11d;
        }
    })();
    function gMul(a, b) {
        if (a === 0 || b === 0) return 0;
        return EXP[(LOG[a] + LOG[b]) % 255];
    }
    function polyMod(data, gen) {
        var res = data.slice();
        for (var i = 0; i <= data.length - gen.length; i++) {
            var c = res[i];
            if (c !== 0) {
                for (var j = 0; j < gen.length; j++) {
                    res[i + j] ^= gMul(gen[j], c);
                }
            }
        }
        return res.slice(data.length - gen.length + 1);
    }
    function genPoly(n) {
        var p = [1];
        for (var i = 0; i < n; i++) {
            var next = new Array(p.length + 1).fill(0);
            for (var j = 0; j < p.length; j++) {
                next[j] ^= gMul(p[j], 1);
                next[j + 1] ^= p[j];
            }
            p = next;
        }
        return p;
    }
    function eccBytes(data, numEc) {
        var gen = genPoly(numEc);
        return polyMod(data.concat(new Array(numEc).fill(0)), gen);
    }

    function toBytes(text) {
        var out = [];
        for (var i = 0; i < text.length; i++) {
            var cp = text.codePointAt(i);
            if (cp > 255) {
                var seq = unescape(encodeURIComponent(text[i]));
                for (var k = 0; k < seq.length; k++) out.push(seq.charCodeAt(k));
            } else {
                out.push(cp & 0xff);
            }
        }
        return out;
    }

    function maskFn(mask, r, c) {
        switch (mask) {
            case 0: return (r + c) % 2 === 0;
            case 1: return r % 2 === 0;
            case 2: return c % 3 === 0;
            case 3: return (r + c) % 3 === 0;
            case 4: return (Math.floor(r / 2) + Math.floor(c / 3)) % 2 === 0;
            case 5: return (r * c) % 2 + (r * c) % 3 === 0;
            case 6: return ((r * c) % 2 + (r * c) % 3) % 2 === 0;
            case 7: return ((r + c) % 2 + (r * c) % 3) % 2 === 0;
        }
        return false;
    }

    function penalty(modules) {
        var size = modules.length, score = 0;
        var run, r, c;
        for (r = 0; r < size; r++) {
            run = 1;
            for (c = 1; c <= size; c++) {
                if (c === size || modules[r][c] !== modules[r][c - 1]) {
                    if (run >= 5) score += 3 + (run - 5);
                    run = 1;
                } else run++;
            }
        }
        for (c = 0; c < size; c++) {
            run = 1;
            for (r = 1; r <= size; r++) {
                if (r === size || modules[r][c] !== modules[r - 1][c]) {
                    if (run >= 5) score += 3 + (run - 5);
                    run = 1;
                } else run++;
            }
        }
        return score;
    }

    function formatBits(ecIndex, mask) {
        var ecBits = [1, 0, 3, 2][ecIndex];
        var data = (ecBits << 3) | mask;
        var rem = data;
        for (var i = 0; i < 10; i++) rem = (rem << 1) ^ ((rem >> 9) * 0x537);
        return ((data << 10) | rem) ^ 0x5412;
    }

    function generate(text, opts) {
        opts = opts || {};
        var ecIndex = { L: 0, M: 1, Q: 2, H: 3 }[opts.ecLevel || 'M'];
        if (ecIndex === undefined) ecIndex = 1;

        var bytes = toBytes(text);
        var len = bytes.length;

        // choose version
        var version = null;
        for (var v = 1; v <= 10; v++) {
            if (len <= DATA[v - 1][ecIndex] - 2) { version = v; break; }
        }
        if (version === null) {
            version = 10;
            if (len > DATA[9][ecIndex] - 2) throw new Error('Data too long for QR code.');
        }

        // Build data codewords (byte mode)
        var bits = [];
        function push(val, n) { for (var i = n - 1; i >= 0; i--) bits.push((val >> i) & 1); }
        push(0x4, 4);
        push(len, version <= 9 ? 8 : 16);
        for (var i = 0; i < bytes.length; i++) push(bytes[i], 8);

        var dataCap = DATA[version - 1][ecIndex];
        var codewords = [];
        for (var i2 = 0; i2 + 7 < bits.length; i2 += 8) {
            var byte = 0;
            for (var b = 0; b < 8; b++) byte = (byte << 1) | bits[i2 + b];
            codewords.push(byte);
        }
        var pads = [0xec, 0x11];
        for (var p = 0; codewords.length < dataCap; p++) codewords.push(pads[p % 2]);

        var numEc = TOTAL[version - 1] - dataCap;
        var ecc = eccBytes(codewords, numEc);
        var all = codewords.concat(ecc);

        var size = 17 + version * 4;

        // reserved function modules
        var reserved = [];
        for (var r = 0; r < size; r++) reserved.push(new Array(size).fill(false));
        function mark(r, c) { if (r >= 0 && r < size && c >= 0 && c < size) reserved[r][c] = true; }
        function markFinder(r0, c0) {
            for (var r = -1; r <= 7; r++) for (var c = -1; c <= 7; c++) mark(r0 + r, c0 + c);
        }
        markFinder(0, 0); markFinder(0, size - 7); markFinder(size - 7, 0);
        for (var t = 8; t < size - 8; t++) { mark(6, t); mark(t, 6); }
        var aligns = ALIGN[version] || [];
        for (var a = 0; a < aligns.length; a++) {
            for (var b2 = 0; b2 < aligns.length; b2++) {
                var ra = aligns[a], ca = aligns[b2];
                if ((ra <= 9 && ca <= 9) || (ra <= 9 && ca >= size - 9) || (ra >= size - 9 && ca <= 9)) continue;
                for (var dr = -2; dr <= 2; dr++) for (var dc = -2; dc <= 2; dc++) mark(ra + dr, ca + dc);
            }
        }
        // format info strips
        for (var f = 0; f <= 8; f++) { if (f !== 6) mark(8, f); if (f !== 6) mark(f, 8); }
        for (var g = 0; g < 8; g++) { mark(size - 1 - g, 8); mark(8, size - 8 + g); }
        // version info
        if (version >= 7) {
            for (var vi = 0; vi < 6; vi++) for (var vj = 0; vj < 3; vj++) { mark(size - 11 + vj, vi); mark(vi, size - 11 + vj); }
        }

        // matrix
        var mods = [];
        for (var r2 = 0; r2 < size; r2++) mods.push(new Array(size).fill(0));
        function set(r, c, val) { mods[r][c] = val ? 1 : 0; }
        function drawFinder(r0, c0) {
            for (var r = 0; r < 7; r++) for (var c = 0; c < 7; c++) {
                set(r0 + r, c0 + c, r === 0 || r === 6 || c === 0 || c === 6 || (r >= 2 && r <= 4 && c >= 2 && c <= 4));
            }
        }
        drawFinder(0, 0); drawFinder(0, size - 7); drawFinder(size - 7, 0);
        for (var t2 = 8; t2 < size - 8; t2++) { set(6, t2, t2 % 2 === 0); set(t2, 6, t2 % 2 === 0); }
        for (var a2 = 0; a2 < aligns.length; a2++) {
            for (var b3 = 0; b3 < aligns.length; b3++) {
                var ra2 = aligns[a2], ca2 = aligns[b3];
                if ((ra2 <= 9 && ca2 <= 9) || (ra2 <= 9 && ca2 >= size - 9) || (ra2 >= size - 9 && ca2 <= 9)) continue;
                for (var dr2 = -2; dr2 <= 2; dr2++) for (var dc2 = -2; dc2 <= 2; dc2++) {
                    set(ra2 + dr2, ca2 + dc2, Math.abs(dr2) === 2 || Math.abs(dc2) === 2 || (dr2 === 0 && dc2 === 0));
                }
            }
        }

        // place data
        var bitIndex = 0, upward = true;
        for (var col = size - 1; col > 0; col -= 2) {
            if (col === 6) col--;
            for (var ri = 0; ri < size; ri++) {
                var row = upward ? size - 1 - ri : ri;
                for (var cc = 0; cc < 2; cc++) {
                    var c2 = col - cc;
                    if (!reserved[row][c2]) {
                        var bit = 0;
                        if (bitIndex < all.length * 8) {
                            var cb = all[Math.floor(bitIndex / 8)];
                            bit = (cb >> (7 - (bitIndex % 8))) & 1;
                        }
                        bitIndex++;
                        mods[row][c2] = bit;
                    }
                }
            }
            upward = !upward;
        }

        // try masks
        var best = null, bestScore = Infinity;
        for (var mask = 0; mask < 8; mask++) {
            var candidate = [];
            for (var r3 = 0; r3 < size; r3++) {
                candidate.push([]);
                for (var c3 = 0; c3 < size; c3++) {
                    var val = mods[r3][c3];
                    if (!reserved[r3][c3] && maskFn(mask, r3, c3)) val = val ? 0 : 1;
                    candidate[r3][c3] = val;
                }
            }
            writeFormat(candidate, size, formatBits(ecIndex, mask), version);
            var sc = penalty(candidate);
            if (sc < bestScore) { bestScore = sc; best = candidate; }
        }

        return { modules: best, size: size };
    }

    function writeFormat(candidate, size, bits, version) {
        // Copy 1 (left/top)
        var copy1 = [
            [8, 0], [8, 1], [8, 2], [8, 3], [8, 4], [8, 5], [8, 7], [8, 8], [7, 8],
            [5, 8], [4, 8], [3, 8], [2, 8], [1, 8], [0, 8]
        ];
        // Copy 2 (bottom-left + top-right)
        var copy2 = [];
        for (var r = size - 1; r >= size - 7; r--) copy2.push([r, 8]);
        copy2.push([size - 8, 7]);
        for (var c = 5; c >= 0; c--) copy2.push([size - 8, c]);

        for (var i = 0; i < 15; i++) {
            var bit = (bits >> (14 - i)) & 1;
            var p1 = copy1[i];
            var p2 = copy2[i];
            candidate[p1[0]][p1[1]] = bit;
            candidate[p2[0]][p2[1]] = bit;
        }
        candidate[size - 8][8] = 1; // dark module

        if (version >= 7) {
            var vbits = VERSION_BITS[version];
            for (var vr = 0; vr < 6; vr++) {
                for (var vc = 0; vc < 3; vc++) {
                    var b = (vbits >> (vr * 3 + vc)) & 1;
                    candidate[size - 11 + vc][vr] = b;
                    candidate[vr][size - 11 + vc] = b;
                }
            }
        }
    }

    function draw(modules, canvas, scale) {
        scale = scale || 4;
        var size = modules.length;
        var px = size * scale;
        canvas.width = px;
        canvas.height = px;
        var ctx = canvas.getContext('2d');
        ctx.fillStyle = '#ffffff';
        ctx.fillRect(0, 0, px, px);
        ctx.fillStyle = '#000000';
        for (var r = 0; r < size; r++) {
            for (var c = 0; c < size; c++) {
                if (modules[r][c]) ctx.fillRect(c * scale, r * scale, scale, scale);
            }
        }
    }

    return {
        generate: function (text, opts) { return generate(text, opts); },
        draw: draw
    };
});

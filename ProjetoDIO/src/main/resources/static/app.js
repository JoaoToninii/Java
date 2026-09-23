// Voice Budget AI - Frontend Controller

let mediaRecorder = null;
let audioChunks = [];
let recordTimerInterval = null;
let recordSeconds = 0;

document.addEventListener('DOMContentLoaded', () => {
    initVoiceRecorder();
    initChatForm();
    initQuickPrompts();
    initFileUpload();
    loadDashboardData();

    document.getElementById('btn-refresh-transacoes').addEventListener('click', loadTransactions);
    document.getElementById('btn-refresh-audit').addEventListener('click', loadAuditLog);
});

// 1. Gravação de Áudio via Microfone do Navegador
function initVoiceRecorder() {
    const btnRecord = document.getElementById('btn-record');
    const statusHint = document.getElementById('record-status-hint');
    const timerDisplay = document.getElementById('record-timer');

    btnRecord.addEventListener('click', async () => {
        if (mediaRecorder && mediaRecorder.state === 'recording') {
            // Parar gravação
            mediaRecorder.stop();
            btnRecord.classList.remove('recording');
            document.getElementById('record-btn-text').textContent = 'Gravar Comando de Voz';
            statusHint.textContent = 'Processando áudio com a IA...';
            clearInterval(recordTimerInterval);
            timerDisplay.style.display = 'none';
        } else {
            // Iniciar gravação
            try {
                const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
                audioChunks = [];
                mediaRecorder = new MediaRecorder(stream);

                mediaRecorder.ondataavailable = (event) => {
                    if (event.data.size > 0) {
                        audioChunks.push(event.data);
                    }
                };

                mediaRecorder.onstop = async () => {
                    const audioBlob = new Blob(audioChunks, { type: 'audio/webm' });
                    // Fechar faixas de áudio para desligar o microfone
                    stream.getTracks().forEach(track => track.stop());
                    await sendAudioToServer(audioBlob, 'comando_voz.webm');
                };

                mediaRecorder.start();
                btnRecord.classList.add('recording');
                document.getElementById('record-btn-text').textContent = 'Parar Gravação';
                statusHint.textContent = 'Gravando... Fale seu comando (ex: "Gastei 50 no almoço")';

                // Timer
                recordSeconds = 0;
                timerDisplay.textContent = '00:00';
                timerDisplay.style.display = 'block';
                recordTimerInterval = setInterval(() => {
                    recordSeconds++;
                    const mins = String(Math.floor(recordSeconds / 60)).padStart(2, '0');
                    const secs = String(recordSeconds % 60).padStart(2, '0');
                    timerDisplay.textContent = `${mins}:${secs}`;
                }, 1000);

            } catch (err) {
                console.error('Erro ao acessar microfone:', err);
                statusHint.textContent = '⚠️ Microfone não permitido ou indisponível. Você pode usar o envio de arquivo ou texto.';
            }
        }
    });
}

// 2. Upload de Arquivo de Áudio
function initFileUpload() {
    const fileInput = document.getElementById('audio-file-input');
    fileInput.addEventListener('change', async (e) => {
        const file = e.target.files[0];
        if (file) {
            document.getElementById('record-status-hint').textContent = `Enviando arquivo: ${file.name}...`;
            await sendAudioToServer(file, file.name);
            fileInput.value = '';
        }
    });
}

// 3. Envio de Áudio para o Backend REST
async function sendAudioToServer(audioBlobOrFile, filename) {
    showLoadingResponse("Transcrevendo e processando áudio...");
    const formData = new FormData();
    formData.append('audio', audioBlobOrFile, filename);

    try {
        const res = await fetch('/api/v1/voice/process', {
            method: 'POST',
            body: formData
        });

        if (!res.ok) {
            throw new Error(`Erro no servidor: ${res.status}`);
        }

        const data = await res.json();
        renderResponse(data);
        loadDashboardData();
    } catch (err) {
        showErrorResponse(`Falha ao processar áudio: ${err.message}`);
    } finally {
        document.getElementById('record-status-hint').textContent = 'Clique para iniciar a gravação pelo microfone';
    }
}

// 4. Envio de Texto via Chat
function initChatForm() {
    const form = document.getElementById('text-chat-form');
    const input = document.getElementById('chat-input');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const texto = input.value.trim();
        if (!texto) return;

        input.value = '';
        await sendTextToServer(texto);
    });
}

async function sendTextToServer(mensagem) {
    showLoadingResponse(`Processando: "${mensagem}"...`);

    try {
        const res = await fetch('/api/v1/voice/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ mensagem })
        });

        if (!res.ok) {
            throw new Error(`Erro HTTP: ${res.status}`);
        }

        const data = await res.json();
        renderResponse(data);
        loadDashboardData();
    } catch (err) {
        showErrorResponse(`Falha ao processar mensagem: ${err.message}`);
    }
}

// 5. Chips de Sugestões Rápidas
function initQuickPrompts() {
    document.querySelectorAll('.chip').forEach(chip => {
        chip.addEventListener('click', () => {
            const prompt = chip.getAttribute('data-prompt');
            sendTextToServer(prompt);
        });
    });
}

// 6. Exibição da Resposta da IA e Áudio TTS
function renderResponse(data) {
    const box = document.getElementById('response-box');
    const transcriptDisplay = document.getElementById('transcript-display');
    const transcriptText = document.getElementById('transcript-text');
    const answerText = document.getElementById('ai-answer-text');
    const timeDisplay = document.getElementById('response-time');
    const actionsWrapper = document.getElementById('actions-executed-wrapper');
    const actionsList = document.getElementById('actions-pill-list');
    const audioWrapper = document.getElementById('audio-player-wrapper');
    const audioPlayer = document.getElementById('tts-audio-player');

    box.style.display = 'block';
    timeDisplay.textContent = `${data.tempoProcessamentoMs || 0} ms`;

    // Transcrição (se canal for áudio)
    if (data.canal === 'AUDIO' && data.textoComando) {
        transcriptDisplay.style.display = 'block';
        transcriptText.textContent = `"${data.textoComando}"`;
    } else {
        transcriptDisplay.style.display = 'none';
    }

    // Texto da resposta
    answerText.textContent = data.respostaIA || "Comando executado com sucesso.";

    // Tool Calling / Ações Executadas
    actionsList.innerHTML = '';
    if (data.acoesExecutadas && data.acoesExecutadas.length > 0) {
        actionsWrapper.style.display = 'block';
        data.acoesExecutadas.forEach(acao => {
            const tag = document.createElement('span');
            tag.className = 'tool-tag';
            tag.textContent = `⚡ ${acao}`;
            actionsList.appendChild(tag);
        });
    } else {
        actionsWrapper.style.display = 'none';
    }

    // Áudio TTS da resposta
    if (data.audioBase64) {
        audioWrapper.style.display = 'block';
        audioPlayer.src = `data:audio/mp3;base64,${data.audioBase64}`;
        audioPlayer.play().catch(e => console.log('Autoplay bloqueado pelo navegador:', e));
    } else {
        audioWrapper.style.display = 'none';
    }
}

function showLoadingResponse(msg) {
    const box = document.getElementById('response-box');
    box.style.display = 'block';
    document.getElementById('transcript-display').style.display = 'none';
    document.getElementById('ai-answer-text').innerHTML = `<em>⏳ ${msg}</em>`;
    document.getElementById('actions-executed-wrapper').style.display = 'none';
    document.getElementById('audio-player-wrapper').style.display = 'none';
}

function showErrorResponse(msg) {
    const box = document.getElementById('response-box');
    box.style.display = 'block';
    document.getElementById('ai-answer-text').innerHTML = `<span style="color: var(--danger);">⚠️ ${msg}</span>`;
}

// 7. Carregamento dos Dados do Dashboard
async function loadDashboardData() {
    await Promise.all([
        loadSaldo(),
        loadCategoryBudgets(),
        loadTransactions(),
        loadAuditLog()
    ]);
}

async function loadSaldo() {
    try {
        const res = await fetch('/api/v1/transacoes/saldo');
        if (res.ok) {
            const saldo = await res.json();
            document.getElementById('kpi-saldo').textContent = formatCurrency(saldo.saldoAtual);
            document.getElementById('kpi-receitas').textContent = formatCurrency(saldo.totalReceitas);
            document.getElementById('kpi-despesas').textContent = formatCurrency(saldo.totalDespesas);
            document.getElementById('kpi-status').textContent = `Status: ${saldo.status}`;
            
            const saldoEl = document.getElementById('kpi-saldo');
            if (saldo.saldoAtual < 0) {
                saldoEl.className = 'kpi-value text-danger';
            } else {
                saldoEl.className = 'kpi-value';
            }
        }
    } catch (e) {
        console.error('Erro ao carregar saldo:', e);
    }
}

async function loadCategoryBudgets() {
    try {
        const res = await fetch('/api/v1/transacoes/resumo-categorias');
        if (res.ok) {
            const lista = await res.json();
            const container = document.getElementById('category-bars-list');
            container.innerHTML = '';

            if (lista.length === 0) {
                container.innerHTML = '<div class="text-muted" style="font-size: 0.85rem;">Nenhum gasto ou orçamento definido ainda.</div>';
                return;
            }

            lista.forEach(cat => {
                const item = document.createElement('div');
                item.className = 'cat-item';

                const pct = cat.porcentagemUtilizada ? Math.min(cat.porcentagemUtilizada, 100) : 0;
                let statusClass = '';
                if (pct >= 100) statusClass = 'danger';
                else if (pct >= 80) statusClass = 'warning';

                const limiteStr = cat.limiteMensal ? ` / ${formatCurrency(cat.limiteMensal)}` : '';
                const pctStr = cat.porcentagemUtilizada ? ` (${cat.porcentagemUtilizada.toFixed(1)}%)` : '';

                item.innerHTML = `
                    <div class="cat-header">
                        <span>${cat.categoria}</span>
                        <span>${formatCurrency(cat.totalGasto)}${limiteStr}${pctStr}</span>
                    </div>
                    <div class="progress-track">
                        <div class="progress-fill ${statusClass}" style="width: ${pct}%"></div>
                    </div>
                    ${cat.alerta ? `<div class="cat-alert">${cat.alerta}</div>` : ''}
                `;
                container.appendChild(item);
            });
        }
    } catch (e) {
        console.error('Erro ao carregar orçamentos por categoria:', e);
    }
}

async function loadTransactions() {
    try {
        const res = await fetch('/api/v1/transacoes?limit=10');
        if (res.ok) {
            const transacoes = await res.json();
            const tbody = document.getElementById('transactions-table-body');
            tbody.innerHTML = '';

            if (transacoes.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Nenhuma transação cadastrada.</td></tr>';
                return;
            }

            transacoes.forEach(t => {
                const tr = document.createElement('tr');
                const isReceita = t.tipo === 'RECEITA';
                tr.innerHTML = `
                    <td>${t.dataTransacao}</td>
                    <td><span class="tipo-tag tipo-${t.tipo}">${t.tipo}</span></td>
                    <td>${t.categoria}</td>
                    <td>${t.descricao}</td>
                    <td class="${isReceita ? 'text-success' : 'text-danger'}" style="font-weight: 700;">
                        ${isReceita ? '+' : '-'} ${formatCurrency(t.valor)}
                    </td>
                `;
                tbody.appendChild(tr);
            });
        }
    } catch (e) {
        console.error('Erro ao carregar transações:', e);
    }
}

async function loadAuditLog() {
    try {
        const res = await fetch('/api/v1/auditoria');
        if (res.ok) {
            const logs = await res.json();
            const container = document.getElementById('audit-list');
            container.innerHTML = '';

            if (logs.length === 0) {
                container.innerHTML = '<div class="text-muted" style="font-size: 0.85rem;">Nenhum registro de auditoria ainda.</div>';
                return;
            }

            logs.slice(0, 8).forEach(item => {
                const div = document.createElement('div');
                div.className = 'audit-item';
                div.innerHTML = `
                    <div class="audit-meta">
                        <span><strong>[${item.canal}]</strong> ${item.intencaoIdentificada || 'COMANDO'}</span>
                        <span>${item.tempoProcessamentoMs}ms &bull; ${item.dataHora ? item.dataHora.replace('T', ' ').substring(0, 19) : ''}</span>
                    </div>
                    <div style="color: #cbd5e1; font-size: 0.82rem;">"${item.entradaUsuario}"</div>
                    ${item.ferramentasChamadas ? `<div style="font-size: 0.72rem; color: #fbbf24; margin-top: 3px;">Tools: ${item.ferramentasChamadas}</div>` : ''}
                `;
                container.appendChild(div);
            });
        }
    } catch (e) {
        console.error('Erro ao carregar auditoria:', e);
    }
}

function formatCurrency(val) {
    const num = parseFloat(val) || 0;
    return num.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

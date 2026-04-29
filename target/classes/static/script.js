let mode = "login";

const authCard = document.getElementById("authCard");
const dashboard = document.getElementById("dashboard");
const authForm = document.getElementById("authForm");
const nomeInput = document.getElementById("nome");
const emailInput = document.getElementById("email");
const senhaInput = document.getElementById("senha");
const message = document.getElementById("message");
const submitBtn = document.getElementById("submitBtn");

function setMode(newMode) {
  mode = newMode;

  document.getElementById("loginTab").classList.remove("active");
  document.getElementById("registerTab").classList.remove("active");

  if (mode === "login") {
    document.getElementById("loginTab").classList.add("active");
    nomeInput.classList.add("hidden");
    submitBtn.innerText = "Entrar";
  } else {
    document.getElementById("registerTab").classList.add("active");
    nomeInput.classList.remove("hidden");
    submitBtn.innerText = "Cadastrar";
  }

  message.innerText = "";
  message.style.color = "#fca5a5";
}

authForm.addEventListener("submit", async function(event) {
  event.preventDefault();

  const payload = {
    nome: nomeInput.value.trim(),
    email: emailInput.value.trim(),
    senha: senhaInput.value.trim()
  };

  if (!payload.email || !payload.senha) {
    message.innerText = "Preencha e-mail e senha.";
    return;
  }

  if (mode === "register" && !payload.nome) {
    message.innerText = "Informe seu nome.";
    return;
  }

  const endpoint = mode === "login" ? "/api/auth/login" : "/api/auth/register";

  submitBtn.innerText = "Carregando...";
  submitBtn.disabled = true;
  message.innerText = "";

  try {
    const response = await fetch(endpoint, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(payload)
    });

    const data = await response.json();

    if (!response.ok) {
      message.style.color = "#fca5a5";
      message.innerText = data.message || "Erro na requisição.";
      return;
    }

    if (mode === "register") {
      message.style.color = "#86efac";
      message.innerText = "Cadastro realizado. Agora faça login.";
      setMode("login");
      return;
    }

    localStorage.setItem("token", data.token);
    showDashboard(data);

  } catch (error) {
    message.style.color = "#fca5a5";
    message.innerText = "Erro ao conectar com o servidor.";
  } finally {
    submitBtn.innerText = mode === "login" ? "Entrar" : "Cadastrar";
    submitBtn.disabled = false;
  }
});

async function checkSession() {
  const token = localStorage.getItem("token");

  if (!token) return;

  try {
    const response = await fetch("/api/auth/me", {
      headers: {
        Authorization: "Bearer " + token
      }
    });

    if (!response.ok) {
      localStorage.removeItem("token");
      return;
    }

    const data = await response.json();
    data.token = token;
    showDashboard(data);

  } catch (error) {
    localStorage.removeItem("token");
  }
}

function showDashboard(data) {
  authCard.classList.add("hidden");
  dashboard.classList.remove("hidden");

  dashboard.style.animation = "fadeUp 0.6s ease";

  document.getElementById("userName").innerText = data.nome;
  document.getElementById("userEmail").innerText = data.email;

  const token = data.token || localStorage.getItem("token");

  document.getElementById("tokenPreview").innerText =
    token.substring(0, 45) + "...";
}

function logout() {
  document.body.style.transition = "0.4s";
  document.body.style.opacity = "0.5";

  setTimeout(() => {
    localStorage.removeItem("token");
    location.reload();
  }, 400);
}

checkSession();
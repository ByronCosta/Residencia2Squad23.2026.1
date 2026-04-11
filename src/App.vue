<template>
  <div id="app">
    <header v-if="usuario" class="header-sistema">
      <h1>🚀 Sistema de Reservas Accenture</h1>
      <div class="user-info">
        <p>Bem-vindo, <strong>{{ nomeExibicao }}</strong>!</p>
        <button @click="logout" class="btn-logout">Sair</button>
      </div>
    </header>
    
    <main v-else class="login-container">
      <h1>Acesso ao Sistema</h1>
      <p>Utilize sua conta corporativa para continuar.</p>
      <a href="http://localhost:8080/saml2/authenticate/azure" class="btn-login">
        Entrar com Microsoft Accenture
      </a>
    </main>

    <router-view v-if="usuario" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { jwtDecode } from 'jwt-decode'

const router = useRouter()
const route = useRoute()
const usuario = ref(null)

// Computed para tratar o nome vindo do JWT (sub ou email)
const nomeExibicao = computed(() => {
  if (!usuario.value) return ""
  // No seu log, o e-mail veio no campo 'sub'
  return usuario.value.name || usuario.value.sub || "Usuário"
})

const carregarUsuario = (token) => {
  try {
    const dados = jwtDecode(token)
    // Verifica se o token não está expirado (exp é em segundos)
    if (dados.exp * 1000 < Date.now()) {
      throw new Error("Token expirado")
    }
    usuario.value = dados
    console.log("Usuário autenticado:", dados.sub)
  } catch (error) {
    console.error("Erro na autenticação:", error.message)
    logout()
  }
}

onMounted(() => {
  // Captura o token da URL após o redirecionamento do Spring
  const urlParams = new URLSearchParams(window.location.search)
  const tokenFromUrl = urlParams.get('token')

  if (tokenFromUrl) {
    localStorage.setItem('user_token', tokenFromUrl)
    carregarUsuario(tokenFromUrl)
    
    // Limpa os parâmetros da URL sem recarregar a página
    window.history.replaceState({}, document.title, window.location.pathname)
  } else {
    const tokenSalvo = localStorage.getItem('user_token')
    if (tokenSalvo) {
      carregarUsuario(tokenSalvo)
    }
  }
})

const logout = () => {
  localStorage.removeItem('user_token')
  usuario.value = null
  // Redireciona para o endpoint de logout do seu Backend
  window.location.href = 'http://localhost:8080/logout'
}
</script>

<style>
#app { font-family: 'Segoe UI', sans-serif; padding: 20px; text-align: center; color: #333; }

.header-sistema { 
  background: #001e60; 
  color: white; 
  padding: 15px 30px; 
  border-radius: 8px; 
  display: flex; 
  justify-content: space-between; 
  align-items: center;
  margin-bottom: 30px;
}

.user-info { display: flex; align-items: center; gap: 20px; }

.btn-logout { 
  cursor: pointer; 
  background: rgba(255,255,255,0.2); 
  color: white; 
  border: 1px solid white; 
  padding: 6px 15px; 
  border-radius: 4px; 
  transition: 0.3s; 
}
.btn-logout:hover { background: #ff4444; border-color: #ff4444; }

.login-container { 
  margin-top: 100px; 
  padding: 40px; 
  border: 1px solid #eee; 
  border-radius: 12px; 
  box-shadow: 0 10px 25px rgba(0,0,0,0.05);
  display: inline-block; 
}

.btn-login { 
  display: inline-block;
  margin-top: 25px;
  text-decoration: none;
  background: #00a1f1; 
  color: white; 
  padding: 14px 28px; 
  border-radius: 6px; 
  font-weight: bold;
  box-shadow: 0 4px 14px rgba(0, 161, 241, 0.4);
  transition: 0.3s;
}
.btn-login:hover { background: #0078d4; transform: translateY(-2px); }
</style>
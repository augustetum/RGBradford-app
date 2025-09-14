import { useState, useEffect } from 'react'
import Footer from './components/footer.jsx'
import Hero from './components/hero.jsx'
import data from './data.js'
import ProjectList from './components/projectList.jsx'
import Project from './components/project.jsx'
import { AnimatePresence, motion } from 'framer-motion';
import Account from './components/account.jsx'
import Upload from './components/upload.jsx'
import Signup from './components/signup.jsx'
import Login from './components/login.jsx'

function App() {
  const [currentProject, setProject] = useState(null)
  const [currentScreen, setCurrentScreen] = useState('signup')    
  const [projects, setProjects] = useState([])
  const [isAuthenticated, setIsAuthenticated] = useState(false)

  useEffect(() => {
    localStorage.removeItem('token');
    setIsAuthenticated(false);
    setCurrentScreen('login');
  }, []);

  const handleLogin = (loginData) => {
    if (loginData && loginData.token) {
      localStorage.setItem('token', loginData.token);
      setIsAuthenticated(true);
      setCurrentScreen('catalog');
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    setIsAuthenticated(false);
    setCurrentScreen('login');
  };

  function handleSwitch(project, newScreen) {
    if (!isAuthenticated && newScreen !== 'login' && newScreen !== 'signup') {
      setCurrentScreen('login');
      alert('Please log in to access this page');
      return;
    }
    switchScreen(project, newScreen)
  }

  const switchScreen = (project, newScreen) => {
    setCurrentScreen(newScreen);
    if (project === currentProject) {setProject(null)}
    else {setProject(project)}
  };

  return (
    <div className='text-center text-igem-white bg-igem-dblue min-h-[100vh] flex flex-col'>
      <div className={`mx-auto w-200 mt-10 mb-35 max-w-[min(90vw,50rem)]`}>
      <AnimatePresence mode="wait">
      <motion.div
       key={currentScreen}
       initial={{ opacity: 0, y: 20 }}
       animate={{ opacity: 1, y: 0 }}
       exit={{ opacity: 0, y: -20 }}
       transition={{ duration: 0.2 }}
      >
      {currentScreen === 'signup' && (
        <Signup setCurrentScreen={setCurrentScreen}/>
      )}
      {currentScreen === 'login' && (
        <Login setCurrentScreen={setCurrentScreen} onLogin={handleLogin}/>
      )}
      {!isAuthenticated && currentScreen !== 'login' && currentScreen !== 'signup' && (
        <Login setCurrentScreen={setCurrentScreen} onLogin={handleLogin}/>
      )}
      {currentScreen === 'catalog' && isAuthenticated && (<>
      <Hero name={data.name}/>
      <ProjectList projects={projects} setProjects={setProjects} handleSwitch={handleSwitch}/>
      </>)}
      {currentScreen === 'project' && isAuthenticated && (
        <Project currentProject={currentProject}/>
      )}
      {currentScreen === 'account' && isAuthenticated && (
        <Account data={data} onLogout={handleLogout}/>
      )}
      {currentScreen === 'upload' && isAuthenticated && (
        <Upload setCurrentScreen={setCurrentScreen}/>
      )}  
      </motion.div>
      </AnimatePresence>
      </div>
      <Footer handleSwitch={handleSwitch}/>
    </div>
  )
}

export default App
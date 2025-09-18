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
  const [notification, setNotification] = useState(null)

  useEffect(() => {
    localStorage.removeItem('token');
    setIsAuthenticated(false);
    setCurrentScreen('login');
  }, []);

  const showNotification = (message, type = 'error', duration = 2000) => {
    setNotification({ message, type, id: Date.now() });
    setTimeout(() => {
      setNotification(null);
    }, duration);
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    setIsAuthenticated(false);
    setCurrentScreen('login');
  };

  function handleSwitch(project, newScreen) {
    if (!isAuthenticated && newScreen !== 'login' && newScreen !== 'signup') {
      setCurrentScreen('login');
      showNotification('Please log in to access this page');
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
      <AnimatePresence>
        {notification && (
          <motion.div
            key={notification.id}
            initial={{ opacity: 0, y: -50 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -50 }}
            transition={{ duration: 0.3 }}
            className={`fixed top-4 left-1/2 transform -translate-x-1/2 z-50 px-4 py-3 rounded-lg shadow-lg max-w-md  ${
              notification.type === 'error' ? 'bg-red-500 text-white' :
              notification.type === 'success' ? 'bg-green-500 text-white' :
              notification.type === 'warning' ? 'bg-yellow-500 text-black' :
              'bg-blue-500 text-white'
            }`}
            onClick={() => setNotification(null)}
          >
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium">{notification.message}</span>
              <button 
                onClick={() => setNotification(null)}
                className="ml-3 text-xl leading-none hover:opacity-70"
              >
                ×
              </button>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

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
        <Login setCurrentScreen={setCurrentScreen} setIsAuthenticated={setIsAuthenticated}/>
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
        <Upload showNotification={showNotification} setCurrentScreen={setCurrentScreen}/>
      )}  
      </motion.div>
      </AnimatePresence>
      </div>
      {isAuthenticated && (<Footer handleSwitch={handleSwitch}/>)}
    </div>
  )
}

export default App
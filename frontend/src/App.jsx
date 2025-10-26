import { useState, useEffect } from 'react'
import Footer from './components/footer.jsx'
import Hero from './components/hero.jsx'
import ProjectList from './components/projectList.jsx'
import Project from './components/project.jsx'
import { AnimatePresence, motion } from 'framer-motion';
import Account from './components/account.jsx'
import Upload from './components/upload.jsx'
import Signup from './components/signup.jsx'
import Login from './components/login.jsx'
import { AccessibilityWidget } from './components/AccessibilityWidget.jsx'
function App() {
  const [currentProject, setProject] = useState(0)
  const [currentScreen, setCurrentScreen] = useState('signup')    
  const [projects, setProjects] = useState([])
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [notification, setNotification] = useState(null)

  useEffect(() => {
    localStorage.removeItem('token');
    setIsAuthenticated(false);
    setCurrentScreen('login');
  }, []);


  const showNotification = (message, type = 'error', duration = 2000, showLoading = false) => {
    const notificationId = Date.now();
    setNotification({ 
      message, 
      type, 
      id: notificationId, 
      loading: showLoading,
      persistent: showLoading
    });
    
    if (!showLoading && duration > 0) {
      setTimeout(() => {
        setNotification(prev => prev?.id === notificationId ? null : prev);
      }, duration);
    }
  };

  const showLoading = (message = 'Loading...') => {
    return showNotification(message, 'loading', 0, true);
  };

  const hideLoading = (resultMessage = null, resultType = 'success', duration = 2000) => {
    if (resultMessage) {
      showNotification(resultMessage, resultType, duration);
    } else {
      setNotification(null);
    }
  };

  const LoadingSpinner = () => (
    <div className="animate-spin h-5 w-5 mr-2">
      <div className="h-full w-full bg-white" style={{
        clipPath: 'polygon(50% 0%, 100% 50%, 50% 100%, 0% 50%)'
      }}></div>
    </div>
  );
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
    setProject(project);
  };

  return (
    <div className='text-center text-igem-white bg-igem-dblue min-h-[100vh] flex flex-col'>
      <AccessibilityWidget />
      <AnimatePresence>
        {notification && (
          <motion.div
            key={notification.id}
            initial={{ opacity: 0, y: -50 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -50 }}
            transition={{ duration: 0.3 }}
            className={`fixed top-4 left-1/2 transform -translate-x-1/2 z-50 px-4 py-3 rounded-lg shadow-lg max-w-md ${
              notification.type === 'error' ? 'bg-red-500 text-white' :
              notification.type === 'success' ? 'bg-green-500 text-white' :
              notification.type === 'warning' ? 'bg-yellow-500 text-black' :
              notification.type === 'loading' ? 'bg-igem-blue text-white' :
              'bg-blue-500 text-white'
            }`}
            onClick={() => !notification.loading && setNotification(null)}
          >
            <div className="flex items-center justify-between">
              <div className="flex items-center">
                {notification.loading && <LoadingSpinner />}
                <span className="text-sm font-medium">{notification.message}</span>
              </div>
              {!notification.loading && (
                <button
                  onClick={() => setNotification(null)}
                  className="ml-3 text-xl leading-none hover:opacity-70"
                >
                  ×
                </button>
              )}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
      
      <div className={`mx-auto w-200 mt-10 mb-45 max-w-[min(90vw,50rem)]`}>
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
      {currentScreen === 'catalog' && isAuthenticated && (<div className="text-base">
      <Hero/>
      <ProjectList setNotification={setNotification} showLoading={showLoading} projects={projects} setProjects={setProjects} handleSwitch={handleSwitch}/>
      </div>)}
      {currentScreen === 'project' && isAuthenticated && (
        <Project project={projects[currentProject]}/>
      )}
      {currentScreen === 'account' && isAuthenticated && (
        <Account onLogout={handleLogout}/>
      )}
      {currentScreen === 'upload' && isAuthenticated && (
        <Upload showNotification={showNotification} showLoading={showLoading} hideLoading={hideLoading} setCurrentScreen={setCurrentScreen}/>
      )}  
      </motion.div>
      </AnimatePresence>
      </div>
      {isAuthenticated && (<Footer handleSwitch={handleSwitch}/>)}
      <footer id="license" className='ignore-accessibility text-base bg-igem-dblue-highlight w-full fixed bottom-0 left-0 right-0 pt-2 h-10 text-center mt-auto'>
      © 2025 - Content on this site is licensed under the GNU Public License.</footer>
    </div>
  )
}

export default App
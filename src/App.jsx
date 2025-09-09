import { useState } from 'react'
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
  const [currentScreen, setCurrentScreen] = useState('login') // catalog
  const projects = data.projects.sort(function (a, b) {
    return Date.parse(b.creationDate) - Date.parse(a.creationDate);
  }) 
  
  function handleSwitch(project, newScreen) {
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
        <Signup />
      )}
      {currentScreen === 'login' && (
        <Login />
      )}
      {currentScreen === 'catalog' && (<>
      <Hero name={data.name}/>
      <ProjectList projects={projects} handleSwitch={handleSwitch}/>
      </>)}
      {currentScreen === 'project' && (
        <Project projects={projects} currentProject={currentProject}/>
      )}
      {currentScreen === 'account' && (
        <Account data={data}/>
      )}
      {currentScreen === 'upload' && (
        <Upload />
      )}  
      </motion.div>
      </AnimatePresence>
      </div>
      <Footer handleSwitch={handleSwitch}/>
    </div>
  )
}

export default App

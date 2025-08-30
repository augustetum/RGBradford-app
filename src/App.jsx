import { useState } from 'react'
import Footer from './components/footer.jsx'
import Hero from './components/hero.jsx'
import data from './data.js'
import ProjectList from './components/projectList.jsx'
import Project from './components/project.jsx'
import { AnimatePresence, motion } from 'framer-motion';

function App() {
  const [currentProject, setProject] = useState(null)
  const [currentScreen, setCurrentScreen] = useState('catalog')
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
    <div className='text-center text-igem-white bg-igem-purple min-h-[100vh] flex flex-col items-center justify-between '>
      <div className={`$screen {isTransitioning ? 'screen-enter' : 'screen-active'}
       mt-10 mb-30 max-w-[min(80vw,50rem)]`}>
      <AnimatePresence mode="wait">
      <motion.div
       key={currentScreen}
       initial={{ opacity: 0, y: 20 }}
       animate={{ opacity: 1, y: 0 }}
       exit={{ opacity: 0, y: -20 }}
       transition={{ duration: 0.2 }}
      >
      {currentScreen === 'catalog' && (<>
      <Hero name={data.name}/>
      <ProjectList projects={projects} handleSwitch={handleSwitch}/>
      </>)}
      {currentScreen === 'project' && (
        <Project projects={projects} currentProject={currentProject}/>
      )}
      </motion.div>
      </AnimatePresence>
      </div>
      <Footer handleSwitch={handleSwitch}/>
    </div>
  )
}

export default App

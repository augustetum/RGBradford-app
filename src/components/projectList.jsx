import React, {useEffect, useState} from "react"

function ProjectList({setProjects, projects, handleSwitch}) {


  const getProjects = async () => {  
    try {
      const token = localStorage.getItem("token")
      const response = await fetch("https://rgbradford-app.onrender.com/api/projects", {
        method: "GET",
        headers: {
          "Authorization": `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        throw new Error(`Failed to fetch projects: ${response.status} ${response.statusText}`);
      }
      
      const data = await response.json();
      setProjects(data);
      console.log(data);
    } catch (error) {
      console.error('Get projects error:', error);
      throw error;
    }
  };

  useEffect(() => {
     getProjects();
  }, []);

  return (
       <main>
        <h2 className='mb-2 text-xl font-semibold text-left'>Recent projects</h2>
        <ul className=''>
          {projects.map((project, i) => (
            <li key={String(project.id) + "project"} onClick={() => handleSwitch(project.id, "project")} className='cursor-pointer hoverRaise flex justify-between rounded-2xl p-2 pl-4 mb-3 bg-igem-white'>
              <div>
                <h3 className='text-left font-semibold text-igem-black'>{project.name}</h3>
                <p className='text-left text-sm text-igem-black opacity-80'>{project.createdAt}</p>
              </div>
              <button className='w-8 flex flex-col gap-1.5 justify-center items-center'>
                {[1,2,3].map((i) => (
                  <div key={String(i) + String(project.id) +"dot"}className='rounded-full w-2 h-2 bg-igem-black'></div>
                ))}
              </button>
            </li>
          ))}
        </ul>
      </main>
      )}
export default ProjectList
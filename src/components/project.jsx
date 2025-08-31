import React from "react";
import plusIcon from '../assets/plus.svg';
function Project({projects, currentProject}) {
    return (
        <main className=''>
        <h1 className='text-3xl font-semibold'>{projects[currentProject].projectTitle}</h1>
        <h3 className='opacity-80 py-4'>{projects[currentProject].creationDate}</h3>
        <button className='cursor-pointer w-[min(80vw,50rem)] h-[30vh] bg-igem-gray 
        rounded-xl flex justify-center items-center'>
            <div>
              <img src={plusIcon} className='mx-auto w-10' />
              <p className=''>upload plate</p>
            </div>
        </button>
        </main>
    )
}

export default Project
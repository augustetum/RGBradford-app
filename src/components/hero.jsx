import React from "react"
import chemistryImage from '../assets/chemistry.png';
function Hero({name}) {

    return(
        <>
        <header className='text-center mt-4 pb-4 text-3xl font-semibold'>
          <h2>Welcome back!</h2>
          
        </header>
        
        <ul className='flex sm:flex-row flex-col items-center justify-center gap-10 my-4'>
          <img src={chemistryImage} alt="" className='drop h-40 w-40' />
          <div className="flex sm:flex-col flex-row gap-10 ">
            <li className=' drop'>
              <a className='' href="protocol.com">
              <div className='hoverRaise elipsoid  px-4 py-8 font-bold bg-igem-white text-igem-black'>
                Read the protocol
              </div>
              </a>
            </li>
            <li className=' drop'>
              <a className='' href="tutorial.com">
              <div className='hoverRaise elipsoid px-4 py-8 font-bold bg-igem-white text-igem-black'>
                Watch the tutorial
              </div>
              </a>
            </li>
          </div>
        </ul>

        </>
    )}

export default Hero
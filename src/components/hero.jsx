import React from "react"
import chemistryImage from '../assets/chemistry.png';
function Hero({name}) {

    return(
        <>
        <header className='text-center pb-4 text-3xl font-semibold'>
          <h2>Welcome back {name}!</h2>
          <img src="src\assets\chemistry.png" alt="" className='drop mx-auto w-auto h-50' />
        </header>
        <ul className='flex flex-col my-4'>
          <li className='mr-auto drop'>
            <a className='' href="protocol.com">
            <div className='hoverRaise elipsoid  px-4 py-8 font-bold bg-igem-white text-igem-black'>
              Read the protocol
            </div>
            </a>
          </li>
          <li className='ml-auto drop'>
            <a className='' href="tutorial.com">
            <div className='hoverRaise elipsoid px-4 py-8 font-bold bg-igem-white text-igem-black'>
              Watch the tutorial
            </div>
            </a>
          </li>
        </ul>

        </>
    )}

export default Hero
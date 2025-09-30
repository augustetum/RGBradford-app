import React from "react"
import folderIcon from "../assets/folder.svg"
import plusIcon from "../assets/plus.svg"
import accountIcon from "../assets/account.svg"

function Footer({handleSwitch}) {
    return (    
    <footer style={{ boxShadow: "0 -0.5rem .5rem rgba(0,0,0,0.5)" }}
    className='drop-shadow-2xl filter max-h-[15vh] h-30 w-full fixed left-0 right-0 bottom-10
    bg-igem-white flex justify-around font-semibold
    text-black py-4 max-w-[50rem] rounded-t-md mx-auto'>
        <button onClick={() => handleSwitch(null, "upload")} className='cursor-pointer h-full text-base'>
        <img className="w-10 mx-auto" src={plusIcon} alt="" />
        <p className='opacity-80'>project</p>
        </button>
        <button onClick={() => handleSwitch(null, "catalog")} className='cursor-pointer h-full text-base'>
        <img className="w-10 mx-auto" src={folderIcon} alt="" />
        <p className='opacity-80'>catalog</p>
        </button>
        <button onClick={() => handleSwitch(null, "account")} className='cursor-pointer h-full text-base'>
        <img className="w-10 mx-auto"  src={accountIcon} alt="" />
        <p className='opacity-80'>account</p>
        </button>
    </footer>
    )}
export default Footer
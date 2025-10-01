import React, {useEffect, useState} from "react"
import { API_BASE_URL } from "../config";

function ProjectList({showLoading, setNotification, setProjects, projects, handleSwitch}) {
  const DEFAULT_SIZE = 20;
  const DEFAULT_SORT = "createdAt,desc";

  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [pageSize, setPageSize] = useState(DEFAULT_SIZE);


  const getProjects = async (page = currentPage, size = pageSize, sort = DEFAULT_SORT) => {
    try {
      showLoading();
      const token = localStorage.getItem("token")
      const response = await fetch(`${API_BASE_URL}/projects?page=${page}&size=${size}&sort=${encodeURIComponent(sort)}`, {
        method: "GET",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Accept": "application/json"
        },
      });

      if (!response.ok) {
        setNotification(null);
        throw new Error(`Failed to fetch projects: ${response.status} ${response.statusText}`);
      }

      const data = await response.json();
      setProjects(data.content || data);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
      setCurrentPage(data.number || page);
      setNotification(null);
    } catch (error) {
      console.error('Get projects error:', error);
      setNotification(null);
      throw error;
    }
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      getProjects(newPage);
    }
  };

  const handlePreviousPage = () => {
    handlePageChange(currentPage - 1);
  };

  const handleNextPage = () => {
    handlePageChange(currentPage + 1);
  };

  useEffect(() => {
     getProjects();
  }, []);

  return (
       <main className="text-base">
        <h2 className='mb-2 text-xl font-semibold text-left'>Recent projects</h2>
        <ul className=''>
          {projects.map((project, i) => (
            <li key={String(project.id) + "project"} onClick={() => handleSwitch(i, "project")} className='cursor-pointer hoverRaise flex justify-between rounded-2xl p-2 pl-4 mb-3 bg-igem-white text-base'>
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

        {totalPages > 1 && (
          <div className="flex justify-center items-center gap-4 mt-6">
            <button
              onClick={handlePreviousPage}
              disabled={currentPage === 0}
              className="px-4 py-2 bg-igem-blue text-white rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-opacity-80 text-base"
            >
              Previous
            </button>

            <span className="text-igem-white text-base">
              Page {currentPage + 1} of {totalPages} ({totalElements} total)
            </span>

            <button
              onClick={handleNextPage}
              disabled={currentPage >= totalPages - 1}
              className="px-4 py-2 bg-igem-blue text-white rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-opacity-80 text-base"
            >
              Next
            </button>
          </div>
        )}
      </main>
      )}
export default ProjectList
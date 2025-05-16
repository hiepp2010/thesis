'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import toast from 'react-hot-toast'
import { getUser, logout } from '@/utils/auth'
import apiClient from '@/utils/api'

export default function Dashboard() {
  const router = useRouter()
  const [userData, setUserData] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [serviceStatus, setServiceStatus] = useState<any>(null)

  useEffect(() => {
    // Check if user is logged in
    const user = getUser()
    
    if (!user) {
      toast.error('You must be logged in to view this page')
      router.push('/login')
      return
    }
    
    setUserData(user)
    
    // Fetch service status
    const fetchServiceStatus = async () => {
      try {
        // Use the API client
        const response = await apiClient.get('/api/chat/public/info')
        setServiceStatus(response.data)
      } catch (error) {
        console.error('Error fetching service status:', error)
        toast.error('Failed to fetch service status')
      } finally {
        setLoading(false)
      }
    }
    
    fetchServiceStatus()
  }, [router])
  
  const handleLogout = () => {
    logout()
    toast.success('Logged out successfully')
    router.push('/')
  }

  if (loading) {
    return (
      <main className="min-h-screen flex flex-col items-center justify-center p-6">
        <div className="text-center">
          <p className="text-xl">Loading...</p>
        </div>
      </main>
    )
  }

  return (
    <main className="min-h-screen flex flex-col items-center justify-center p-6">
      <div className="bg-white dark:bg-slate-800 p-8 rounded-lg shadow-md w-full max-w-md">
        <h1 className="text-2xl font-bold mb-6 text-center">Dashboard</h1>
        
        {userData && (
          <div className="mb-6">
            <h2 className="text-xl font-semibold mb-2">User Information</h2>
            <p><strong>Username:</strong> {userData.username}</p>
            <p><strong>Email:</strong> {userData.email}</p>
            <p><strong>Roles:</strong> {userData.roles.join(', ')}</p>
          </div>
        )}
        
        {serviceStatus && (
          <div className="mb-6">
            <h2 className="text-xl font-semibold mb-2">Service Status</h2>
            <p><strong>Status:</strong> {serviceStatus.status}</p>
            <p><strong>Visibility:</strong> {serviceStatus.visibility}</p>
            <p><strong>User:</strong> {serviceStatus.user}</p>
          </div>
        )}
        
        <button 
          onClick={handleLogout}
          className="btn btn-primary w-full"
        >
          Logout
        </button>
      </div>
    </main>
  )
} 
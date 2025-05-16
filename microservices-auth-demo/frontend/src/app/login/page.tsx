'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import toast from 'react-hot-toast'
import apiClient from '@/utils/api'

export default function Login() {
  const router = useRouter()
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [successMsg, setSuccessMsg] = useState<string | null>(null)
  const [formData, setFormData] = useState({
    username: '',
    password: ''
  })

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    })
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsLoading(true)
    setError(null)
    setSuccessMsg(null)

    try {
      // Use the API client instead of direct axios
      const response = await apiClient.post('/api/auth/authenticate', formData)
      
      // Set success message
      setSuccessMsg('Login successful!')
      
      // Store the token
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('user', JSON.stringify({
        username: response.data.username,
        email: response.data.email,
        roles: response.data.roles
      }))
      
      toast.success('Login successful!')
      
      // Redirect to dashboard after successful login
      setTimeout(() => {
        router.push('/dashboard')
      }, 1000)
    } catch (error: any) {
      console.error("Login error:", error)
      const errorMessage = error.response 
        ? `Status: ${error.response.status}, Message: ${JSON.stringify(error.response.data)}`
        : error.message || 'Unknown error';
        
      setError(`Login failed. ${errorMessage}`)
      toast.error('Login failed. Please check your credentials.')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <main className="min-h-screen flex flex-col items-center justify-center p-6">
      <div className="bg-white dark:bg-slate-800 p-8 rounded-lg shadow-md w-full max-w-md">
        <h1 className="text-2xl font-bold mb-6 text-center">Login</h1>
        
        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label htmlFor="username" className="block mb-2 text-sm font-medium">
              Username
            </label>
            <input
              type="text"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              className="input"
              required
            />
          </div>
          
          <div className="mb-6">
            <label htmlFor="password" className="block mb-2 text-sm font-medium">
              Password
            </label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              className="input"
              required
            />
          </div>
          
          <button 
            type="submit" 
            className="btn btn-primary w-full"
            disabled={isLoading}
          >
            {isLoading ? 'Logging in...' : 'Login'}
          </button>
        </form>
        
        {error && (
          <div className="mt-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
            <p className="font-bold">Error:</p>
            <p className="break-words">{error}</p>
          </div>
        )}
        
        {successMsg && (
          <div className="mt-4 p-3 bg-green-100 border border-green-400 text-green-700 rounded">
            <p className="font-bold">Success:</p>
            <p className="break-words">{successMsg}</p>
          </div>
        )}
        
        <div className="mt-4 text-center">
          <p>
            Don't have an account?{' '}
            <Link href="/register" className="text-primary hover:underline">
              Register
            </Link>
          </p>
        </div>
      </div>
    </main>
  )
} 
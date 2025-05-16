'use client'

import { useState } from 'react'
import Link from 'next/link'

export default function Home() {
  return (
    <main className="min-h-screen flex flex-col items-center justify-center p-6">
      <div className="text-center max-w-4xl mx-auto">
        <h1 className="text-4xl font-bold mb-6">Microservices Auth Demo</h1>
        <p className="text-xl mb-8">
          Demo application showcasing authorization in a microservices architecture
        </p>
        
        <div className="flex gap-4 justify-center">
          <Link href="/login" className="btn btn-primary">
            Login
          </Link>
          <Link href="/register" className="btn btn-secondary">
            Register
          </Link>
        </div>
      </div>
    </main>
  )
} 